package com.thirdchannel.rabbitmq.exceptions;

public class RPCException extends Exception {
    public RPCException(String exchange, String key, Throwable cause) {
        super("RPC exception on exchange " + exchange + " on topic " + key, cause);
    }
}
