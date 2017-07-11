package com.thirdchannel.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import com.thirdchannel.rabbitmq.exceptions.RabbitMQSetupException;
import net.jodah.lyra.Connections;
import net.jodah.lyra.config.*;
import net.jodah.lyra.util.Duration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class LyraConnector {

    private static final Duration RABBITMQ_RECOVERY_INTERVAL = Duration.milliseconds(100);
    private static final Duration RABBITMQ_MAX_RECOVERY_INTERVAL = Duration.seconds(10);
    private static final Duration RABBITMQ_RETRY_INTERVAL = Duration.milliseconds(100);
    private static final Duration RABBITMQ_MAX_RETRY_INTERVAL = Duration.seconds(10);

    private static final Config config = new Config()
        .withRecoveryPolicy(new RecoveryPolicy()
            .withBackoff(RABBITMQ_RECOVERY_INTERVAL, RABBITMQ_MAX_RECOVERY_INTERVAL))
        .withRetryPolicy(new RetryPolicy()
            .withBackoff(RABBITMQ_RETRY_INTERVAL, RABBITMQ_MAX_RETRY_INTERVAL)
        );

    public static ConfigurableConnection newConnection(ConnectionFactory factory) throws RabbitMQSetupException {
        try {
            return Connections.create(factory, config);
        } catch (IOException | TimeoutException e) {
            throw new RabbitMQSetupException("Could not create connection", e);
        }
    }
}
