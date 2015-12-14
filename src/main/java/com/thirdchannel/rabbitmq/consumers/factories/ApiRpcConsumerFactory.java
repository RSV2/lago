package com.thirdchannel.rabbitmq.consumers.factories;

import com.thirdchannel.rabbitmq.consumers.ApiRpcConsumer;

/**
 * @author Steve Pember
 */
public class ApiRpcConsumerFactory extends LagoConsumerFactory<ApiRpcConsumer> {
    @Override
    public ApiRpcConsumer produceConsumer() {
        return new ApiRpcConsumer();
    }
}
