package com.thirdchannel.rabbitmq

import com.thirdchannel.rabbitmq.config.RabbitMQConfig
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Steve Pember
 */
class PropertiesManagerSpec extends Specification {

    @Shared PropertiesManager manager

    void setup() {
        manager = new PropertiesManager()
    }

    def "Should load properties from YAML" () {
        when:
        RabbitMQConfig config = manager.load()

        then:
        config.connectionUrl.equals("test")
        config.exchanges.size() == 2
        config.exchanges[0].name.equals("oneTopic")
        config.exchanges[0].type.equals("topic")

        config.exchanges[1].type.equals("direct")
        config.exchanges[1].name.equals("mailbox")
    }

    def "Properties not in config should use the defaults in RabbitConfig, while extraneous properties are ignored"() {
        when:
        RabbitMQConfig config = manager.load()

        then:
        config.connectionUrl.equals("test")
        config.connectionTimeout == 10000;
    }
}
