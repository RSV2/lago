package com.thirdchannel.rabbitmq.mock

import com.thirdchannel.rabbitmq.RabbitMQDeliveryDetails
import com.thirdchannel.rabbitmq.consumers.LagoEventConsumer
import spock.util.concurrent.BlockingVariable

/**
 * @author Steve Pember
 */
class WidgetConsumer extends LagoEventConsumer<Widget> {
    BlockingVariable<Widget> latestWidget = new BlockingVariable<Widget>()

    @Override
    boolean handleMessage(Widget message, RabbitMQDeliveryDetails rabbitMQDeliveryDetails) {
        log.info("Received widget");
        //latestWidget = message;
        latestWidget.set(message)
    }
}
