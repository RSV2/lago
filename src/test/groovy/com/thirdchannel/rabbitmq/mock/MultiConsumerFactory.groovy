package com.thirdchannel.rabbitmq.mock

import com.thirdchannel.rabbitmq.consumers.EventConsumer
import com.thirdchannel.rabbitmq.consumers.factories.LagoConsumerFactory

/**
 * @author Steve Pember
 */
class MultiConsumerFactory extends LagoConsumerFactory<WidgetConsumer> {

    @Override
    WidgetConsumer produceConsumer() {
        return new WidgetConsumer()
    }
}
