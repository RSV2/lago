package com.thirdchannel.rabbitmq.interfaces;

import com.thirdchannel.rabbitmq.RabbitMQDeliveryDetails;

/**
 * @author Steve Pember
 */
public interface RpcConsumer<M, R> {

    R handleRPC(M message, RabbitMQDeliveryDetails rabbitMQDeliveryDetails);

    Class<R> getResponseClass();
}
