package com.thirdchannel.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import com.thirdchannel.rabbitmq.exceptions.RabbitMQSetupException;
import net.jodah.lyra.Connections;
import net.jodah.lyra.config.Config;
import net.jodah.lyra.config.ConfigurableConnection;
import net.jodah.lyra.config.RecoveryPolicies;
import net.jodah.lyra.config.RetryPolicy;
import net.jodah.lyra.util.Duration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class LyraConnector {

    private static final int MAX_RECONNECTION_ATTEMPTS = 20;
    private static final Duration RABBITMQ_RECONNECTION_INTERVAL = Duration.seconds(1);
    private static final Duration MAX_RABBITMQ_RECONNECTION_INTERVAL = Duration.minutes(5);

    private static Config config = new Config()
        .withRecoveryPolicy(RecoveryPolicies.recoverAlways())
        .withRetryPolicy(new RetryPolicy()
            .withMaxAttempts(MAX_RECONNECTION_ATTEMPTS)
            .withInterval(RABBITMQ_RECONNECTION_INTERVAL)
            .withMaxDuration(MAX_RABBITMQ_RECONNECTION_INTERVAL)
        );

    public static ConfigurableConnection newConnection(ConnectionFactory factory) throws RabbitMQSetupException {
        try {
            return Connections.create(factory, config);
        } catch (IOException | TimeoutException e) {
            throw new RabbitMQSetupException("Could not create connection", e);
        }
    }
}
