package com.thirdchannel.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.rabbitmq.client.*;
import com.thirdchannel.rabbitmq.config.ExchangeConfig;
import com.thirdchannel.rabbitmq.config.QueueConsumerConfig;
import com.thirdchannel.rabbitmq.config.RabbitMQConfig;
import com.thirdchannel.rabbitmq.exceptions.LagoDefaultExceptionHandler;
import com.thirdchannel.rabbitmq.interfaces.EventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Will keep a main channel open for publishing, although one can publish with an additional channel
 *
 * @author Steve Pember
 */
public class Lago implements com.thirdchannel.rabbitmq.interfaces.Lago {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    public ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public void setObjectMapper(ObjectMapper mapper) {
        OBJECT_MAPPER = mapper;
    }
    
    protected void loadConfig() {

        try {
            config = propertiesManager.load();
        } catch (FileNotFoundException e) {
            log.error("No config file 'lago.yaml' found on the classpath");
        }
    }

    public RabbitMQConfig getConfig() {
        return config;
    }

    public void registerConsumer(EventConsumer consumer) {
        if (consumer.isConfigured()) {
            log.info(consumer.getClass().getSimpleName() +" appears to be already configured");
        } else {
            consumer.setConfig(config.findQueueConfig(consumer));
        }
        if (consumer.getConfig().getCount() > 0) {
            log.debug("About to spin up " + consumer.getConfig().getCount() + " instances of " + consumer.getClass().getSimpleName());
            bindConsumer(consumer, 0);
            for (int i = 1; i < consumer.getConfig().getCount(); i++) {
                bindConsumer(consumer.spawn(), i);
            }
            log.info("Registered Consumer: " + consumer.getClass().getSimpleName());
        } else {
            log.warn("Count of less then one provided for Consumer: " + consumer.getClass().getSimpleName());
        }
    }

    private void bindConsumer(EventConsumer consumer, int count) {
        consumer.setChannel(createChannel());
        consumer.setQueueName(consumer.getConfig().getName());

        try {
            log.debug("About to make queue with name: " + consumer.getQueueName());
            Channel channel = consumer.getChannel();

            QueueConsumerConfig queueConsumerConfig = consumer.getConfig();

            channel.basicQos(queueConsumerConfig.getPrefetch());

            channel.queueDeclare(
                    consumer.getQueueName(),
                    queueConsumerConfig.isDurable(),
                    queueConsumerConfig.getCount() > 1,
                    queueConsumerConfig.isAutoDelete(),
                    null
            );

            consumer.setLago(this);

            for(String key : queueConsumerConfig.getKeys()) {
                // bind the queue to each key
                channel.queueBind(consumer.getQueueName(), queueConsumerConfig.getExchangeName(), key);
            }

            // but ony one bind for the consumer in general
            channel.basicConsume(
                    consumer.getQueueName(),
                    queueConsumerConfig.isAutoAck(),
                    consumer.getClass().getSimpleName() + "-" + (count + 1),
                    consumer
            );

            registeredConsumers.add(consumer);
        } catch (IOException e) {
            log.error("Could not declare queue and bind to consumer: " + e.getMessage(), e);
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
     * Connects usinsee ConnectionFacory}, allowing for custom configuration by the service.
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
            // todo: declare internal api rpc consumer
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

    public Channel getChannel() {
        return channel;
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
     * @param message Object containing the information you want to transmit. Could be as simple as a single value, a Map, an Object, etc. This object will be serialized using Jackson, so Jackson Annotations will be respected
     * @param key String The routing key for outgoing message
     * @param properties BasicProperties Standard RabbitMQ Basic Properties
     * @param channel Channel The Channel to transmit on
     * @param exchangeName String The name of the exchange to transmit on
     */
    public void publish(String exchangeName, String key, Object message, AMQP.BasicProperties properties, Channel channel) {
        try {
            log.debug("Publishing to exchange '{}' with key '{}'", exchangeName, key);
            channel.basicPublish(exchangeName, key, properties, OBJECT_MAPPER.writeValueAsString(message).getBytes());
        } catch(IOException ioe) {
            log.error("Failed to publish message: ", ioe);
        }

    }


    /**
     *
     * @param exchangeName The name of the exchange to publish on
     * @param key String The routing key to publish on
     * @param message Object representing the outgoing data. Will typically encapsulate some sort of query information
     * @param clazz Clazz The class of the expected return data
     * @param channel Channel Channel to broadcast on
     * @return Object Will be an instance of clazz
     * @throws IOException If unable to connect or bind the queuetion
     */
    public Object rpc(String exchangeName, String key, Object message, Class clazz, Channel channel) throws IOException {
        return rpc(exchangeName, key, message, clazz, channel, UUID.randomUUID().toString(), null);
    }

    /**
     *
     * @param exchangeName The name of the exchange to publish on
     * @param key String The routing key to publish on
     * @param message Object representing the outgoing data. Will typically encapsulate some sort of query information
     * @param clazz Clazz The class of the expected return data
     * @param channel Channel Channel to broadcast on
     * @param rpcTimeout Integer in millis of a custom timeout for a particular RPC
     * @return Object Will be an instance of clazz
     * @throws IOException If unable to connect or bind the queuetion
     */
    public Object rpc(String exchangeName, String key, Object message, Class clazz, Channel channel, Integer rpcTimeout) throws IOException {
        return rpc(exchangeName, key, message, clazz, channel, UUID.randomUUID().toString(), rpcTimeout);
    }

    /**
     *
     * @param exchangeName The name of the exchange to publish on
     * @param key String The routing key to publish on
     * @param message Object representing the outgoing data. Will typically encapsulate some sort of query information
     * @param clazz Clazz The class of the expected return data
     * @param channel Channel Channel to broadcas
     * @param traceId A unique identifier for tracing communications on
     * @param rpcTimeout Integer in millis of a custom timeout for a particular RPC
     * @return Object Will be an instance of clazz
     * @throws IOException If unable to connect or bind the queuetion
     */
    public Object rpc(String exchangeName, String key, Object message, Class clazz, Channel channel, String traceId, Integer rpcTimeout) throws IOException {
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
        RpcStopWatch stopWatch = null;
        if (config.isLogRpcTime()) {stopWatch = new RpcStopWatch().start();}

        ObjectReader objectReader = OBJECT_MAPPER.readerFor(clazz);
        String replyQueueName = channel.queueDeclare("", false, false, true, null).getQueue();
        log.info("Listening for rpc response on " + replyQueueName);

        QueueingConsumer consumer = new QueueingConsumer(channel);

        channel.queueBind(replyQueueName, exchangeName, replyQueueName);
        channel.basicConsume(replyQueueName, true, consumer);

        RabbitMQDeliveryDetails rpcDetails = buildRpcRabbitMQDeliveryDetails(exchangeName, key, replyQueueName, traceId, rpcTimeout);
        log.debug("Expiration for RPC: " + rpcDetails.getBasicProperties().getExpiration());

        // then publish the query
        publish(exchangeName, key, message, rpcDetails.getBasicProperties(), channel);
        log.debug("Waiting for rpc response delivery on " + key);

        QueueingConsumer.Delivery delivery = null;
        try {
            delivery = consumer.nextDelivery(chooseTimeout(rpcTimeout));
        } catch (InterruptedException e) {
            log.error("Thread interrupted while waiting for rpc response:", e);
            delivery = null;
        }

        if (delivery != null) {
            log.trace("RPC response received.");
            if (delivery.getProperties().getCorrelationId().equals(rpcDetails.getBasicProperties().getCorrelationId())) {
                log.trace("Correlation ids are equal.");
                channel.basicCancel(consumer.getConsumerTag());
//
            } else {
                log.warn("Correlation ids not equal! key: " + key);
                return null;
            }
        } else {
            log.warn("Timeout occurred on RPC message to key: " + key);
            return null;
        }
//        // we must clean up!
        channel.queueUnbind(replyQueueName, exchangeName, replyQueueName);
        channel.queueDelete(replyQueueName);
        if (config.isLogRpcTime() && stopWatch != null) {
            stopWatch.stopAndPublish(rpcDetails);
        }
        log.debug("Received: {}", new String(delivery.getBody()));
        return objectReader.readValue(delivery.getBody());
    }

    private int chooseTimeout(Integer timeoutOverride) {
        if(timeoutOverride != null) {
            return timeoutOverride;
        } else {
            return config.getRpcTimeout();
        }
    }

    private RabbitMQDeliveryDetails buildRpcRabbitMQDeliveryDetails(String exchangeName, String key, String replyQueueName, String traceId, Integer rpcTimeout ) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(RpcStopWatch.TRACE_ID, traceId);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(UUID.randomUUID().toString())
                .replyTo(replyQueueName)
                .headers(headers)
                .build();

        if(rpcTimeout != null) {
            props = props.builder().expiration(rpcTimeout.toString()).build();
        }

        return new RabbitMQDeliveryDetails(new Envelope(0, true, exchangeName, key), props, "temp-rpc");
    }
}
