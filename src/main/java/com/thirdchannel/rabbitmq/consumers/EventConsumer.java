package com.thirdchannel.rabbitmq.consumers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.thirdchannel.rabbitmq.RabbitMQDeliveryDetails;
import com.thirdchannel.rabbitmq.interfaces.Lago;

/**
 * @author Steve Pember
 */
public interface EventConsumer<M> extends Consumer {

    void setChannel(Channel channel);
    Channel getChannel();

    void setAutoAck(boolean autoAck);
    boolean isAutoAck();

    void setLago(Lago lago);
    Lago getLago();

    Class<M> getMessageClass();

    boolean handleMessage(M data, RabbitMQDeliveryDetails rabbitMQDeliveryDetails);

    /**
     * Like 'copy', but meant to be used to handle things like dependency injection;
     * @return
     */
    EventConsumer<M> spawn();

}
