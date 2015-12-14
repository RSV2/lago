package com.thirdchannel.rabbitmq.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.thirdchannel.rabbitmq.RabbitMQDeliveryDetails;
import com.thirdchannel.rabbitmq.interfaces.Lago;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 *
 * @author Steve Pember
 */
abstract public class LagoEventConsumer<M> implements EventConsumer<M> {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    private Lago lago;
    private Channel channel;
    private boolean autoAck = false;
    private String exchangeName;

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
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public void setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
    }

    @Override
    public boolean isAutoAck() {
        return autoAck;
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
            if (!handleMessage(OBJECT_MAPPER.readValue(body, getMessageClass()), deliveryDetails)) {
                deliveryStatus = false;
            }
        }
        catch(NullPointerException e) {
            log.error("Uncaught NPE: " + e.getMessage(), e);
        }

        decideToAck(envelope, deliveryStatus);
        log.debug("Message consumed");

    }

    private void decideToAck(Envelope envelope, boolean deliveryStatus) throws IOException {
        if (!isAutoAck()) {
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
