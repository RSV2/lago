package com.thirdchannel.rabbitmq.consumers

import com.thirdchannel.rabbitmq.Lago
import com.thirdchannel.rabbitmq.config.QueueConsumerConfig
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


        QueueConsumerConfig config = new QueueConsumerConfig(exchangeName: "oneTopic", key: "api:lago", count: 1 )
        apiRpcConsumer = new ApiRpcConsumer()
        apiRpcConsumer.setConfig(config)
    }

    void "test" () {
        given:
        lago.registerConsumer(new WidgetConsumer())
        lago.registerConsumer(new WidgetRPCConsumer())
        lago.registerConsumer(apiRpcConsumer)

        when:

        Map result = apiRpcConsumer.buildApi()

        then:
        result != null

        // service
        // topic
        // exchange
        // in
        //  - fieldname -> type
        // out

    }
}