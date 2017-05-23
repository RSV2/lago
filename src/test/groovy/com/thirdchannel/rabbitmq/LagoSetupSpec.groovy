package com.thirdchannel.rabbitmq
import com.thirdchannel.rabbitmq.exceptions.RabbitMQSetupException
import com.thirdchannel.rabbitmq.mock.MultiConsumer
import com.thirdchannel.rabbitmq.mock.WidgetConsumer
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Steve Pember
 */
class LagoSetupSpec extends Specification {
    private class BadConsumer extends WidgetConsumer {
    }

    @Shared Lago lago

    def setup() {
        lago = new Lago();
        lago.connect("guest", "guest", "/", "localhost", 5672)
    }

    def cleanup() {
        lago.close()
    }

    void "Consumers present in the configuration should receive the applied configuration settings" () {
        given:
        WidgetConsumer consumer = new WidgetConsumer()

        when:
        lago.registerConsumer(consumer)

        then:
        consumer.config.autoDelete
        !consumer.config.durable
        consumer.config.count == 1
        consumer.config.keys == ["foo.bar"]
        consumer.config.name == "test1"
        consumer.config.prefetch == 1
        consumer.getQueueName() == "test1"
        lago.getRegisteredConsumers().size() == 1
    }

    void "Consumers with counts > 1 should create multiple consumers" () {
        when:
        lago.registerConsumer(new WidgetConsumer())
        lago.registerConsumer(new MultiConsumer())

        then:
        lago.getRegisteredConsumers().size() == 4
    }

    void "Consumers not present in the configuration should receive no config" () {
        given:
        BadConsumer consumer = new BadConsumer()

        when:
        lago.registerConsumer(consumer)

        then:
        !consumer.config.autoDelete
        !consumer.config.autoAck
        !consumer.config.backwardsCompatible
        consumer.config.prefetch == 1
        consumer.config.durable
        consumer.config.count == 0
        consumer.config.keys == []
    }

    void "Connecting without specifiying informmation should use the config or environment var "() {
        given:
        lago = new Lago()

        when:
        lago.connect()

        then:
        lago.getConnection() != null;
    }

    void "Connecting with a bad url should throw a setupexception" () {
        given:
        lago = new Lago()

        when:
        lago.connect("foo.blah")

        then:
        thrown RabbitMQSetupException
    }
}
