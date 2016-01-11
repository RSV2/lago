package com.thirdchannel.rabbitmq.interfaces;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.thirdchannel.rabbitmq.RabbitMQDeliveryDetails;
import com.thirdchannel.rabbitmq.config.QueueConsumerConfig;

/**
 * @author Steve Pember
 */
public interface EventConsumer<M> extends Consumer {

    String getQueueName();
    void setQueueName(String name);

    void setChannel(Channel channel);
    Channel getChannel();

    void setLago(Lago lago);
    Lago getLago();

    void setConfig(QueueConsumerConfig queueConsumerConfig);
    QueueConsumerConfig getConfig();
    boolean isConfigured();

    Class<M> getMessageClass();

    boolean handleMessage(M data, RabbitMQDeliveryDetails rabbitMQDeliveryDetails);

    /**
     * Like 'copy', but meant to be used to handle things like dependency injection;
     * @return EventConsumer a duplicate of the original
     */
    EventConsumer<M> spawn();

}
