package com.thirdchannel.rabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.thirdchannel.rabbitmq.mock.MockChannel
import com.thirdchannel.rabbitmq.mock.Widget
import com.thirdchannel.rabbitmq.mock.WidgetConsumer
import com.thirdchannel.rabbitmq.mock.WidgetRPCConsumer
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Steve Pember
 */
class LagoSpec extends Specification {

    @Shared Lago lago
    void setup() {
        lago = new Lago()
        lago.connect("guest", "guest", "/", "localhost", 5672)
    }

    void cleanup() {
        lago.close()
    }


    def "Basic publish check" () {
        given:
        MockChannel channel = new MockChannel()
        Widget widget = new Widget();
        widget.name = "test"

        when:
        lago.publish("test", "foo.bar", widget, new AMQP.BasicProperties(), channel)
        List<String> data = channel.getPublishedData("foo.bar")

        then:
        data[0] == '{"count":0,"active":true,"name":"test"}'
    }

    def "publishing an object should serialize it to JSON"() {
        given:
        MockChannel channel = new MockChannel()

        expect:
        lago.publish("test", "foo.bar", object, new AMQP.BasicProperties(), channel)
        channel.getPublishedData("foo.bar")[0] == result

        where:
        object                              |   result
        new Widget(10, false, "FizzBuzz")   |   '{"count":10,"active":false,"name":"FizzBuzz"}'
        [foo:1, fizz: "buzz"]               |   '{"foo":1,"fizz":"buzz"}'
        [["foo":1], ["foo":2]]              |   '[{"foo":1},{"foo":2}]'
    }

    def "Live publish test" () {

        given:

        Widget widget = new Widget(count: 100, active:true, name: "Blarg")
        lago.registerConsumer(new WidgetConsumer())

        when:
        lago.publish("oneTopic", "foo.bar", widget, new AMQP.BasicProperties())

        then:
        Widget latest = ((WidgetConsumer)lago.getRegisteredConsumers()[0]).getLatestWidget().get()
        latest.count == 100
        latest.active
        latest.name == "Blarg"

    }

    def "Rpc calls should send and receive JSON" () {

        given:
        lago.registerConsumer(new WidgetRPCConsumer());
        lago.registerConsumer(new WidgetConsumer());


        when:
        Channel channel = lago.createChannel()
        Widget widget = (Widget)lago.rpc("oneTopic", "widget.read", [widgetId: 6], Widget.class, channel)
        channel.close()

        then:
        widget != null
        widget.active
        widget.count == 5
        widget.name == "RPC Test Widget"
    }
}
