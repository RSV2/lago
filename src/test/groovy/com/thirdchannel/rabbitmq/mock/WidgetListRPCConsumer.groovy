package com.thirdchannel.rabbitmq.mock

import com.thirdchannel.rabbitmq.RabbitMQDeliveryDetails
import com.thirdchannel.rabbitmq.LagoRpcConsumer


class WidgetListRPCConsumer extends LagoRpcConsumer<WidgetListQuery, List<Widget>>{
    @Override
    List<Widget> handleRPC(WidgetListQuery message, RabbitMQDeliveryDetails rabbitMQDeliveryDetails) {
        log.info("Recevied message ${message} with ids ${message.widgetIds}")
        return [new Widget(4, true, 'RPC test Widget'), new Widget(5, true, "RPC Test Widget")]
    }
}

class WidgetListQuery {
    List<Integer> widgetIds;
}
