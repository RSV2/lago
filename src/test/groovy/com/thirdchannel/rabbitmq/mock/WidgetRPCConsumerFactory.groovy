package com.thirdchannel.rabbitmq.mock

import com.thirdchannel.rabbitmq.consumers.factories.LagoConsumerFactory

/**
 * @author Steve Pember
 */
class WidgetRPCConsumerFactory extends LagoConsumerFactory<WidgetRPCConsumer> {
    @Override
    WidgetRPCConsumer produceConsumer() {
        return new WidgetRPCConsumer()
    }
}
