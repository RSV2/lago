package com.thirdchannel.rabbitmq.mock

import com.thirdchannel.rabbitmq.consumers.factories.LagoConsumerFactory

/**
 * @author Steve Pember
 */
class WidgetConsumerFactory extends LagoConsumerFactory<WidgetConsumer> {
    @Override
    WidgetConsumer produceConsumer() {
        return new WidgetConsumer();
    }
}
