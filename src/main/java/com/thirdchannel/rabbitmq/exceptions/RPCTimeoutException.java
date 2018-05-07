package com.thirdchannel.rabbitmq.exceptions;

public class RPCTimeoutException extends Exception {
    public RPCTimeoutException(String topic) {
        super("RPC timeout on topic " + topic);
    }
}
