package com.thirdchannel.rabbitmq

import com.thirdchannel.rabbitmq.consumers.factories.LagoConsumerFactory
import com.thirdchannel.rabbitmq.mock.MultiConsumerFactory
import com.thirdchannel.rabbitmq.mock.WidgetConsumer
import com.thirdchannel.rabbitmq.mock.WidgetConsumerFactory
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Steve Pember
 */
class LagoSetupSpec extends Specification {
    private class BadConsumerFactory extends LagoConsumerFactory<WidgetConsumer> {
        @Override
        WidgetConsumer produceConsumer() {
            throw new RuntimeException("Should not be called!");
        }
    }

    @Shared Lago lago

    def setup() {
        lago = new Lago();
        lago.connect("guest", "guest", "/", "localhost", 5672)
    }

    def cleanup() {
        lago.close()
    }

    void "Consumer Factories present in the configuration should receive the applied configuration settings" () {
        given:
        WidgetConsumerFactory factory = new WidgetConsumerFactory()

        when:
        lago.registerConsumerFactory(factory)

        then:
        factory.autoDelete
        !factory.durable
        factory.count == 1
        factory.key == "foo.bar"
        lago.getRegisteredConsumers().size() == 1
    }

    void "Consumer factories with counts > 1 should create multiple consumers" () {
        when:
        lago.registerConsumerFactory(new WidgetConsumerFactory())
        lago.registerConsumerFactory(new MultiConsumerFactory())

        then:
        lago.getRegisteredConsumers().size() == 4
    }

    void "Consumer Factories not present in the configuration should receive no config" () {
        given:
        BadConsumerFactory factory = new BadConsumerFactory()

        when:
        lago.registerConsumerFactory(factory)

        then:
        !factory.autoDelete
        !factory.durable
        factory.count == 0
        factory.key == ""
    }

    void "Connecting without specifiying informmation should use the config or environment var "() {
        given:
        lago = new Lago()

        when:
        lago.connect()

        then:
        lago.getConnection() != null;
    }

    void "Connecting with a bad url should return null for the connection" () {
        given:
        lago = new Lago()

        when:
        lago.connect("foo.blah")

        then:
        lago.getConnection() == null
    }
}
