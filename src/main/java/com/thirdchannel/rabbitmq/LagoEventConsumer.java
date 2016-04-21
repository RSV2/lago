package com.thirdchannel.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.thirdchannel.rabbitmq.config.QueueConsumerConfig;
import com.thirdchannel.rabbitmq.interfaces.EventConsumer;
import com.thirdchannel.rabbitmq.interfaces.Lago;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 *
 * @author Steve Pember
 */
abstract public class LagoEventConsumer<M> implements EventConsumer<M>, Cloneable {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    private Lago lago;
    private Channel channel;
    private QueueConsumerConfig config;
    private String queueName = this.getClass().getSimpleName().toLowerCase();

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private Class<M> messageType;




    @SuppressWarnings("unchecked")
    private void setMessageType() {
        messageType = ((Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        log.trace("Set generic type of " + messageType.getSimpleName());
    }

    @Override
    public Class<M> getMessageClass() {
        if (messageType == null) {
            setMessageType();
        }
        return messageType;
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    public void setQueueName(String name) {
        queueName = name;
    }



    @Override
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }


    @Override
    public void setConfig(QueueConsumerConfig queueConsumerConfig) {
        this.config = queueConsumerConfig;
    }

    @Override
    public QueueConsumerConfig getConfig() {
        return config;
    }

    @Override
    public boolean isConfigured() {
        return getConfig() != null && !StringUtils.isEmpty(getQueueName()) && !StringUtils.isEmpty(config.getExchangeName()) && config.getKeys().size() > 0;
    }


    @Override
    public void setLago(Lago lago) {
        this.lago = lago;
    }

    @Override
    public Lago getLago() {
        return lago;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {

    }

    @Override
    public void handleCancelOk(String consumerTag) {
        log.warn("Handling Cancel Ok: " + consumerTag);
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        log.warn("Handling Cancel: " + consumerTag);
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        log.warn("Shutting down: " + consumerTag + ": " + sig.getReason());
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        log.debug("Recovered : " + consumerTag);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Handling message with topic " + envelope.getRoutingKey() + " on consumerTag " + consumerTag +" and data: " + new String(body));
        }

        RabbitMQDeliveryDetails deliveryDetails = new RabbitMQDeliveryDetails(envelope, properties, consumerTag);

        boolean deliveryStatus = true;

        try {
            if (!handleMessage(getObjectMapper().readValue(body, getMessageClass()), deliveryDetails)) {
                deliveryStatus = false;
            }
        }
        catch(NullPointerException e) {
            log.error("Uncaught NPE: " + e.getMessage(), e);
        }

        decideToAck(envelope, deliveryStatus);
        log.debug("Message consumed");

    }

    public ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    private void decideToAck(Envelope envelope, boolean deliveryStatus) throws IOException {
        if (!config.isAutoAck()) {
            if (deliveryStatus) {
                channel.basicAck(envelope.getDeliveryTag(), false);
            } else {
                channel.basicNack(envelope.getDeliveryTag(), false, true);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public EventConsumer<M> spawn() {
        try {
            return (EventConsumer<M>) this.clone();
        } catch (CloneNotSupportedException e) {
            log.error("Could not spawn new consumer: ", e);
            return null;
        }
    }
}
