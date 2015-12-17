package com.thirdchannel.rabbitmq

import com.rabbitmq.client.Channel
import com.thirdchannel.rabbitmq.mock.Widget
import com.thirdchannel.rabbitmq.mock.WidgetConsumer
import com.thirdchannel.rabbitmq.mock.WidgetRPCConsumer
import spock.lang.Shared
import spock.lang.Specification


/**
 * Tests a variety of Rpc Consumer actions
 *
 * @author Steve Pember
 */
class LagoRpcConsumerGeneralSpec extends Specification {

    @Shared Lago lago
    void setup() {
        lago = new Lago()
        lago.connect("guest", "guest", "/", "localhost", 5672)
    }

    void cleanup() {
        lago.close()
    }

    def "Disabling time logging shouldn't cause problems" () {
        given:
        WidgetRPCConsumer consumer = new WidgetRPCConsumer()
        lago.registerConsumer(consumer)
        consumer.getConfig().setLogTime(false)

        when:
        Channel channel = lago.createChannel()
        Widget widget = (Widget)lago.rpc("oneTopic", "widget.read", [widgetId: 6], Widget.class, channel)
        channel.close()

        then:
        widget != null
        widget.count == 5
    }

}