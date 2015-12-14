package com.thirdchannel.rabbitmq.consumers

import com.thirdchannel.rabbitmq.Lago
import com.thirdchannel.rabbitmq.consumers.factories.ApiRpcConsumerFactory
import com.thirdchannel.rabbitmq.mock.WidgetConsumerFactory
import com.thirdchannel.rabbitmq.mock.WidgetRPCConsumerFactory
import spock.lang.Shared
import spock.lang.Specification


/**
 * @author Steve Pember
 */
class ApiRpcConsumerSpec extends Specification {

    @Shared ApiRpcConsumerFactory apiRpcConsumerFactory
    @Shared Lago lago

    def setup() {
        lago = new Lago();
        lago.connect()

        apiRpcConsumerFactory = new ApiRpcConsumerFactory(queueName: "api-lago", exchangeName: "oneTopic", key: "api:lago", count: 1)
    }

    void "test" () {
        given:
        lago.registerConsumerFactory(new WidgetConsumerFactory())
        lago.registerConsumerFactory(new WidgetRPCConsumerFactory())
        lago.registerConsumerFactory(apiRpcConsumerFactory)

        when:

        Map result = ((ApiRpcConsumer)lago.getRegisteredConsumers()[3]).buildApi()

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