package com.thirdchannel.rabbitmq.exceptions;

public class RabbitMQSetupException extends Exception {
    public RabbitMQSetupException(Throwable cause) {
        super(cause);
    }
    public RabbitMQSetupException(String message, Throwable cause) {
        super(message, cause);
    }
    public RabbitMQSetupException(String message) {
        super(message);
    }
}
