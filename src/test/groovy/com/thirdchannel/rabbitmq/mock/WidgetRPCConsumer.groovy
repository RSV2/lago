package com.thirdchannel.rabbitmq.mock

import com.thirdchannel.rabbitmq.RabbitMQDeliveryDetails
import com.thirdchannel.rabbitmq.LagoRpcConsumer

/**
 * @author Steve Pember
 */
class WidgetRPCConsumer extends LagoRpcConsumer<WidgetQuery, Widget>{
    @Override
    Widget handleRPC(WidgetQuery message, RabbitMQDeliveryDetails rabbitMQDeliveryDetails) {
        log.info("Recevied message ${message} with id of ${message.widgetId}")
        return new Widget(5, true, "RPC Test Widget")
    }
}

class WidgetQuery {
    int widgetId;
}
