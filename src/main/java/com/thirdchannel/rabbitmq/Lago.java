package com.thirdchannel.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.rabbitmq.client.*;
import com.thirdchannel.rabbitmq.config.ExchangeConfig;
import com.thirdchannel.rabbitmq.config.RabbitMQConfig;
import com.thirdchannel.rabbitmq.consumers.factories.ConsumerFactory;
import com.thirdchannel.rabbitmq.consumers.EventConsumer;
import com.thirdchannel.rabbitmq.exceptions.LagoDefaultExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Will keep a main channel open for publishing, although one can publish with an additional channel
 *
 * @author Steve Pember
 */
public class Lago implements com.thirdchannel.rabbitmq.interfaces.Lago {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Connection connection;
    private Channel channel; // create a local channel just for Lago

    private ConnectionFactory connectionFactory;
    private ExceptionHandler exceptionHandler = new LagoDefaultExceptionHandler();

    private final List<EventConsumer> registeredConsumers = new ArrayList<EventConsumer>();

    private RabbitMQConfig config;
    private PropertiesManager propertiesManager = new PropertiesManager();

    public Lago() {
        loadConfig();
    }


    protected void loadConfig() {
        try {
            config = propertiesManager.load();
        } catch (FileNotFoundException e) {
            log.error("No config file 'lago.yaml' found on the classpath");
        } catch (URISyntaxException e) {
            log.error("Invalid URI signature for the path to the config file.");
        }
    }


    public void registerConsumerFactory(ConsumerFactory factory) {
        /*
            given a factory, looks for the matching configuration in the rabbitMqConfig
            Configuration specifies the exchange, the queues, the key they listen on ( this could potentially lead to trouble in a 'direct' environment)
            the details about the queue, the consumerFactories's
         */
        factory.applyConfig(config.findQueueConfigForFactory(factory));
        log.info("About to spin up " + factory.getCount() + "instances.");
        for (int i = 0; i < factory.getCount(); i++) {
            EventConsumer consumer = factory.produceConsumer();
            log.info("Have consumer " + consumer.getClass());
            consumer.setChannel(createChannel());
            try {
                log.info("About to make queue with name: " + factory.getQueueName());
                consumer.getChannel().queueDeclare(
                        factory.getQueueName(),
                        factory.isDurable(),
                        factory.getCount() > 1,
                        factory.isAutoDelete(),
                        null
                );
                consumer.setLago(this);
                consumer.getChannel().queueBind(factory.getQueueName(), factory.getExchangeName(), factory.getKey());
                consumer.getChannel().basicConsume(factory.getQueueName(), true,
                        consumer.getClass().getSimpleName() + "-" + (i + 1), consumer);
                registeredConsumers.add(consumer);
            } catch (IOException e) {
                log.error("Could not declare queue and bind to consumer: " + e.getMessage(), e);
            }
        }
    }


    public List<EventConsumer> getRegisteredConsumers() {
        return registeredConsumers;
    }


    public Connection connect () {
        // if environment variable present, use that
        // otherwise, use config. if no config, then throw exception
        String connectionUrl = config.getConnectionEnvironmentUrl();
        if (!connectionUrl.isEmpty()) {
            connect(connectionUrl);
        } else if (config.hasConnectionConfig()) {
            connect(config.getUsername(), config.getPassword(), config.getVirtualHost(), config.getHost(), config.getPort());
        } else {
            throw new RuntimeException("Could not located rabbit mq configuration in environment or config");
        }
        return getConnection();

    }

    public Connection connect(String url) {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setUri(url);
            return connect(factory);
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException | NullPointerException e) {
            log.error("Could not connect to Rabbit over url " + url + ": ", e);
            return null;
        }
    }

    public Connection connect(String userName, String password, String virtualHost, String host, int port) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(userName);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        factory.setHost(host);
        factory.setPort(port);
        factory.setConnectionTimeout(config.getConnectionTimeout());
        return connect(factory);
    }

    /**
     * Connects using a {@see ConnectionFactory}, allowing for custom configuration by the service.
     * Warning: no configuration will be provided. Make sure that you've set values like automatic recovery
     *
     * @param factory the factory
     * @return a connection
     */
    public Connection connect(ConnectionFactory factory) {
        try {
            defaultFactorySettings(factory, config);
            connectionFactory = factory;
            connection = factory.newConnection();
            log.info("Connected to Rabbit");
            channel = createChannel();
            log.debug("Declaring exchanges");
            for (ExchangeConfig exchangeConfig : config.getExchanges()) {
                channel.exchangeDeclare(exchangeConfig.getName(), exchangeConfig.getType(), exchangeConfig.isDurable(), exchangeConfig.isAutoDelete(), null);
            }
        } catch(IOException | TimeoutException e) {
            log.error(e.getMessage(), e);
        }
        return connection;
    }

    /**
     * Sets initial defaults for the factory during connection.
     */
    private void defaultFactorySettings(ConnectionFactory factory, RabbitMQConfig config) {
        // the Java client for Rabbit has inconsistent settings for timing values. e.g. second vs milliseconds

        factory.setRequestedHeartbeat(config.getHeartbeatInterval());
        factory.setConnectionTimeout(config.getConnectionTimeout());
        factory.setAutomaticRecoveryEnabled(config.isAutomaticRecoveryEnabled());
        factory.setTopologyRecoveryEnabled(config.isTopologyRecoveryEnabled());
        factory.setExceptionHandler(exceptionHandler);
    }


    public Channel createChannel() {
        try {
            return connection.createChannel();
        } catch (IOException e) {
            log.error("Could not create channel: ", e);
            return null;
        }
    }

    public void setExceptionHandler(ExceptionHandler handler) {
        exceptionHandler = handler;
    }

    public void close() {
        try {
            channel.close();
            connection.close();
        } catch (IOException | TimeoutException | NullPointerException e) {
            log.error("Could not close connection: ", e);
        }
    }


    @Override
    public Connection getConnection() {
        return connection;
    }

    public void publish(String exchangeName, String key, Object message, AMQP.BasicProperties properties) {
        publish(exchangeName, key, message, properties, this.channel);
    }

    /**
     *
     * @param message
     * @param key
     * @param properties
     * @param channel
     * @param exchangeName
     */
    public void publish(String exchangeName, String key, Object message, AMQP.BasicProperties properties, Channel channel) {
        try {
            log.debug("Publishing to exchange '{}' with key '{}'", exchangeName, key);
            channel.basicPublish(exchangeName, key, properties, OBJECT_MAPPER.writeValueAsString(message).getBytes());
        } catch(IOException ioe) {
            log.error("Failed to publish message: ", ioe);
        }

    }


    public Object rpc(String exchangeName, String key, Object message, Class clazz, Channel channel) throws IOException {
        // to do an RPC (synchronous, in this case) in RabbitMQ, we must do the following:
        // 1. create a unique response queue for the rpc call
        // 2. create a new channel for the queue //todo: eventually make this optional
        // 3. define a response correlation id. create a basic properties object with the response id
        // 4. publish
        // 5. wait for the response on the unique queue. if timeout, prepare empty response
        // 6. destroy unique queue
        // 7. return response
        // Also, allow configuration for logging response times, or timeouts on rpc calls
        //
        //
        // Ok, furthermore, the RabbitMq java library has implementations of RPC and AsyncRPC on the channel class.
        // Assuming they do what I think they do, they would be amazing to use. However:
        // * I cannot find any documentation on how to use them, all searches for things like 'rabbitmq java client channel rpc' result in
        //      documentation about how to programatically do an rpc call (e.g. what we do here).
        // * The official java rabbitmq documentation also says to do what we do here.
        ObjectReader objectReader = OBJECT_MAPPER.readerFor(clazz);
        String replyQueueName = channel.queueDeclare().getQueue();
        log.info("Listening for rpc response on " + replyQueueName);

        QueueingConsumer consumer = new QueueingConsumer(channel);

        channel.queueBind(replyQueueName, exchangeName, replyQueueName);
        channel.basicConsume(replyQueueName, true, consumer);

        String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();
        // then publish the query
        publish(exchangeName, key, message, props, channel);
        log.debug("Waiting for rpc response delivery on " + key);

        QueueingConsumer.Delivery delivery = null;
        try {
            delivery = consumer.nextDelivery(config.getRpcTimeout());
        } catch (InterruptedException e) {
            log.error("Thread interrupted while waiting for rpc response:", e);
            delivery = null;
        }

        if (delivery != null) {
            log.debug("RPC response received.");

            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                log.debug("Correlation ids are equal.");
                channel.basicCancel(consumer.getConsumerTag());
//
            } else {
                log.warn("Correlation ids not equal!");
            }
        } else {
            log.warn("Timeout occurred on RPC message to topic: " + key);
            return null;
        }
//        // we must clean up!
        channel.queueUnbind(replyQueueName, exchangeName, replyQueueName);
        channel.queueDelete(replyQueueName);
        //parse response;
        return objectReader.readValue(delivery.getBody());
    }
}
