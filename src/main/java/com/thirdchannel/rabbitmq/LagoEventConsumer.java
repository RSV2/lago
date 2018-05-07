package com.thirdchannel.rabbitmq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.thirdchannel.rabbitmq.config.QueueConsumerConfig;
import com.thirdchannel.rabbitmq.exceptions.RabbitMQSetupException;
import com.thirdchannel.rabbitmq.interfaces.EventConsumer;
import com.thirdchannel.rabbitmq.interfaces.Lago;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

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
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        if(log.isInfoEnabled()) {
            log.info(
                "Handling message with topic {} on consumerTag {} and data: {}",
                envelope.getRoutingKey(),
                consumerTag,
                new String(body)
            );
        }
        RabbitMQDeliveryDetails deliveryDetails = new RabbitMQDeliveryDetails(envelope, properties, consumerTag);
        boolean deliveryStatus = false;
        try {
            ObjectMapper objectMapper = getObjectMapper();
            Class messageClass = getMessageClass();
            JsonNode rootJsonNode = objectMapper.readTree(body);

            if(getConfig().isBackwardsCompatible()) {
                if (rootJsonNode.isObject()) {
                    List<JsonNode> children = new ArrayList<>();
                    children.add(rootJsonNode);
                    rootJsonNode = new ArrayNode(objectMapper.getNodeFactory(), children);
                }
                Boolean anyFalse = false;
                for (JsonNode n : rootJsonNode) {
                    M value = (M) objectMapper.treeToValue(n, messageClass);
                    if(!handleMessage(value, deliveryDetails)) {
                        anyFalse = true;
                    }
                }
                if(!anyFalse) {
                    deliveryStatus = true;
                }
            } else {
                M value = (M) objectMapper.treeToValue(rootJsonNode, messageClass);
                if (handleMessage(value, deliveryDetails)) {
                    deliveryStatus = true;
                }
            }

        } catch(Exception e) {
            log.error("Exception occurred while attempting to handle message on topic {}:", envelope.getRoutingKey(), e);
        }
        decideToAck(envelope, deliveryStatus);
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
