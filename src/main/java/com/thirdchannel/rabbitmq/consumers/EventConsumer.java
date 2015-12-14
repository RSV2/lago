package com.thirdchannel.rabbitmq.consumers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.thirdchannel.rabbitmq.RabbitMQDeliveryDetails;
import com.thirdchannel.rabbitmq.config.QueueConsumerConfig;
import com.thirdchannel.rabbitmq.interfaces.Lago;

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
     * @return
     */
    EventConsumer<M> spawn();

}
