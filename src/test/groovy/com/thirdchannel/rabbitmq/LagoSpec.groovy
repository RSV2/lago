package com.thirdchannel.rabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.thirdchannel.rabbitmq.mock.MockChannel
import com.thirdchannel.rabbitmq.mock.Widget
import com.thirdchannel.rabbitmq.mock.WidgetConsumer
import com.thirdchannel.rabbitmq.mock.WidgetListRPCConsumer
import com.thirdchannel.rabbitmq.mock.WidgetRPCConsumer
import com.thirdchannel.rabbitmq.mock.*
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.util.logging.Slf4j
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable
/**
 * @author Steve Pember
 */
 @Slf4j
class LagoSpec extends Specification {

    @Shared Lago lago
    void setup() {
        lago = new Lago()
        lago.connect("guest", "guest", "/", "localhost", 5672)
    }

    void cleanup() {
        lago.close()
    }


    @Ignore
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

    @Ignore
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

        when:
        lago.registerConsumer(new WidgetConsumer())

        then:
        lago.getRegisteredConsumers().size() == 1

        when:
        WidgetConsumer consumer = ((WidgetConsumer)lago.getRegisteredConsumers().first())
        lago.publish("oneTopic", "foo.bar", widget, new AMQP.BasicProperties())
        BlockingVariable<Widget> latestWidget = consumer.getLatestWidget()

        then:
        Widget latest = latestWidget.get()
        latest.count == 100
        latest.active
        latest.name == "Blarg"

    }

    @Ignore
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

    @Ignore
    def "Rpc calls should send and receive JSON collections" () {

        given:
        lago.registerConsumer(new WidgetListRPCConsumer());
        lago.registerConsumer(new WidgetConsumer());


        when:
        Channel channel = lago.createChannel()
        List<Widget> widgets = (List<Widget>)lago.rpc("oneTopic", "widgets.read", [widgetIds: [4, 5]], List.class, Widget.class, channel)
        channel.close()

        then:
        widgets != null
        List.class.isAssignableFrom(widgets.class)
        Widget.class.isInstance(widgets[0])
        widgets[0].count == 4
        widgets[1].count == 5
    }

    @Ignore
    void "RPCs can have custom timeouts"(){
        given:
        lago.registerConsumer(new TimeoutRPCConsumer())

        when:
        Channel channel = lago.createChannel()
        Date start = new Date()
        try {
            lago.rpc("oneTopic", 'timeout.read', [widgetId: 6], null, Widget.class, channel, 1000)
        } catch(Exception e) {
            log.info("Catching deliberate rpc timeout exception")
        }
        Date end = new Date()

        then:
        TimeDuration td = TimeCategory.minus( end, start )
        td.seconds == 1

    }
}
