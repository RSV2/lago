package com.thirdchannel.rabbitmq.consumers

import com.thirdchannel.rabbitmq.ApiRpcConsumer
import com.thirdchannel.rabbitmq.Lago
import com.thirdchannel.rabbitmq.config.QueueConsumerConfig
import com.thirdchannel.rabbitmq.messages.ApiResponse
import com.thirdchannel.rabbitmq.mock.WidgetConsumer
import com.thirdchannel.rabbitmq.mock.WidgetRPCConsumer
import spock.lang.Shared
import spock.lang.Specification


/**
 * @author Steve Pember
 */
class ApiRpcConsumerSpec extends Specification {

    ApiRpcConsumer apiRpcConsumer
    @Shared Lago lago

    def setup() {
        lago = new Lago();
        lago.connect()


        QueueConsumerConfig config = new QueueConsumerConfig(exchangeName: "oneTopic", keys: ["api:lago"], count: 1, prefetch: 2, name: "foo")
        apiRpcConsumer = new ApiRpcConsumer()
        apiRpcConsumer.setConfig(config)
    }

    void "test" () {
        given:
        lago.registerConsumer(new WidgetConsumer())
        lago.registerConsumer(new WidgetRPCConsumer())
        lago.registerConsumer(apiRpcConsumer)

        when:

        //ApiResponse response = apiRpcConsumer.buildApi()
        ApiResponse response = lago.rpc("oneTopic", "api:lago", [:], ApiResponse.class, lago.channel)
        println "Have consumers of ${response.consumers.size()}"
        println response
        response.consumers.each {
            println it.in
            println it.out
        }

        then:
        response != null;

        // service
        // topic
        // exchange
        // in
        //  - fieldname -> type
        // out

    }
}
