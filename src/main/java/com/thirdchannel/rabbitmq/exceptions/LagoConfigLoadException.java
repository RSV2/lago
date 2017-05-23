package com.thirdchannel.rabbitmq.exceptions;

public class LagoConfigLoadException extends Exception {
    public LagoConfigLoadException(Throwable cause) {
        super(cause);
    }
    public LagoConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }
    public LagoConfigLoadException(String message) {
        super(message);
    }
}
