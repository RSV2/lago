package com.thirdchannel.rabbitmq.mock

import com.thirdchannel.rabbitmq.LagoRpcConsumer
import com.thirdchannel.rabbitmq.RabbitMQDeliveryDetails

/**
 * @author mlittle
 *
 * This consumer is used to validate that rpcs can have custom timeouts
 */
class TimeoutRPCConsumer extends LagoRpcConsumer<WidgetQuery, Widget> {
    @Override
    Widget handleRPC(WidgetQuery message, RabbitMQDeliveryDetails rabbitMQDeliveryDetails) {
        log.info("Recevied message ${message} with id of ${message.widgetId}")

        log.info("going to sleep!")
        sleep(2000) // sleeping so that custom rpc timeouts can be validated

        return new Widget(5, true, "RPC Test Widget")
    }
}