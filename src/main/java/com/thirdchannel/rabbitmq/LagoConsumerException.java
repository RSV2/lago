package com.thirdchannel.rabbitmq;

public class LagoConsumerException extends Exception {
    public LagoConsumerException(final String message) {
        super(message);
    }

    public LagoConsumerException(
        final String message,
        final Throwable cause
    ) {
        super(
            message,
            cause
        );
    }
}
