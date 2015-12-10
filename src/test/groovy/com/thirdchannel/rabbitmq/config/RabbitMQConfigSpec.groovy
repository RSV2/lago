package com.thirdchannel.rabbitmq.config

import spock.lang.Specification


/**
 * @author Steve Pember
 */
class RabbitMQConfigSpec extends Specification {



    void "#hasConnectionConfig should return false if any piece of vial info is missing" () {
        given:
        RabbitMQConfig config = new RabbitMQConfig()

        expect:
        config.setUsername(username)
        config.setPassword(pw)
        config.setHost(host)
        config.hasConnectionConfig() == result

        where:
        username    |   pw      | host              |   result
        "user1"     |   null    |   "localhost"     |   false
        "user1"     |   ""      |   "localhost"     |   false
        "user1"     |   "blah"  |   null            |   false
        "user1"     |   "blah"  |   ""              |   false
        null        |   "pass"  |   "localhost"     |   false
        ""          |   "pass"  |   "localhost"     |   false
        "user1"     |   "pass"  |   "localhost"     |   true
    }

    void "Getters and setters. slave to coverage" () {
        given:
        RabbitMQConfig config = new RabbitMQConfig()

        when:
        config.setAutomaticRecoveryEnabled(false)
        config.setTopologyRecoveryEnabled(true)
        config.setHeartbeatInterval(125)
        config.setRpcTimeout(1000)
        config.setConnectionTimeout(1000)
        config.setVirtualHost("/foo")
        config.setPort(4000)
        config.setUsername("tester")

        then:
        !config.isAutomaticRecoveryEnabled()
        config.isTopologyRecoveryEnabled()
        config.getHeartbeatInterval() == 125
        config.getRpcTimeout() == 1000
        config.getConnectionTimeout() == 1000
        config.getVirtualHost() == "/foo"
        config.getPort() == 4000
        config.getUsername() == "tester"
    }

}