package com.thirdchannel.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.thirdchannel.rabbitmq.config.QueueConsumerConfig;
import com.thirdchannel.rabbitmq.exceptions.RabbitMQSetupException;
import com.thirdchannel.rabbitmq.interfaces.EventConsumer;
import com.thirdchannel.rabbitmq.interfaces.Lago;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

abstract public class LagoEventConsumer<M> implements EventConsumer<M>, Cloneable {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    private Lago lago;
    private Channel channel;
    private QueueConsumerConfig config;
    private String queueName = this.getClass().getSimpleName().toLowerCase();

    private Class<M> messageType;




    @SuppressWarnings("unchecked")
    private void setMessageType() {
        messageType = ((Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
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
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        if(log.isInfoEnabled()) {
            log.info(
                "Handling message with topic {} on consumerTag {} and data: {}",
                envelope.getRoutingKey(),
                consumerTag,
                new String(body, UTF_8)
            );
        }
        RabbitMQDeliveryDetails deliveryDetails = new RabbitMQDeliveryDetails(envelope, properties, consumerTag);
        try {
            final ObjectMapper objectMapper = getObjectMapper();
            final JsonNode rootJsonNode;
            try {
                rootJsonNode = objectMapper.readTree(body);
            } catch (final IOException e) {
                throw new LagoConsumerException("Could not read message as JSON", e);
            }

            final boolean deliveryStatus;
            if (rootJsonNode.isObject()) {

                final M message = deserialize(objectMapper, rootJsonNode);
                deliveryStatus = handleMessage(message, deliveryDetails);

            } else if (rootJsonNode.isArray()) {

                boolean allSuccessfullyHandled = true;
                for(final JsonNode jsonElement : rootJsonNode) {
                    final M message = deserialize(objectMapper, jsonElement);
                    final boolean successfullyHandled = handleMessage(message, deliveryDetails);
                    if(!successfullyHandled) {
                        allSuccessfullyHandled = false;
                    }
                }
                deliveryStatus = allSuccessfullyHandled;

            } else{
                throw new LagoConsumerException("Received message JSON that was not an Object or Array");
            }

            decideToAck(envelope, deliveryStatus);

        } catch(final Exception e) {

            if (log.isErrorEnabled()) {
                log.error(
                    "Exception occurred while attempting to handle message on topic {}: {}",
                    envelope.getRoutingKey(),
                    new String(body, UTF_8),
                    e
                );
            }

        }
    }

    private M deserialize(ObjectMapper objectMapper, JsonNode json) throws LagoConsumerException {
        Class<M> messageClass = getMessageClass();
        try {
            return objectMapper.treeToValue(json, messageClass);
        } catch (JsonProcessingException e) {
            throw new LagoConsumerException("Could not deserialize JSON into " + messageClass.getCanonicalName(), e);
        }
    }

    public ObjectMapper getObjectMapper() {
        return getLago().getObjectMapper();
    }

    private void decideToAck(Envelope envelope, boolean deliveryStatus) throws IOException {
        if (!config.isAutoAck()) {
            if (deliveryStatus) {
                channel.basicAck(envelope.getDeliveryTag(), false);
            } else {
                channel.basicReject(envelope.getDeliveryTag(), false);
            }
        }
    }

    @Override
    public EventConsumer<M> spawn() throws RabbitMQSetupException {
        try {
            return (EventConsumer<M>) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new RabbitMQSetupException("Could not clone consumer", e);
        }
    }
}
