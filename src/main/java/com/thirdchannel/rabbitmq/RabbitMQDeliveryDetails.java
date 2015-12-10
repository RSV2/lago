package com.thirdchannel.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

/**
 * @author Steve Pember
 */
public class RabbitMQDeliveryDetails {
    private String consumerTag;
    private Envelope envelope;
    private AMQP.BasicProperties basicProperties;

    public RabbitMQDeliveryDetails(Envelope envelope, AMQP.BasicProperties properties, String consumerTag) {
        this.consumerTag = consumerTag;
        this.envelope = envelope;
        this.basicProperties = properties;
    }

    public String getConsumerTag() {
        return consumerTag;
    }

    public void setConsumerTag(String consumerTag) {
        this.consumerTag = consumerTag;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    public AMQP.BasicProperties getBasicProperties() {
        return basicProperties;
    }

    public void setBasicProperties(AMQP.BasicProperties basicProperties) {
        this.basicProperties = basicProperties;
    }
}
