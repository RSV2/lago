package com.thirdchannel.rabbitmq.interfaces;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * @author Steve Pember
 */
public interface Lago {

    void close();

    Connection getConnection();
    Connection connect();
    Connection connect(String url);
    Connection connect(String userName, String password, String virtualHost, String host, int port);
    Connection connect(ConnectionFactory factory);
    Channel createChannel();

    void setExceptionHandler(ExceptionHandler handler);

    void publish(String exchangeName, String key, Object message, AMQP.BasicProperties properties);
    void publish(String exchangeName, String key, Object message, AMQP.BasicProperties properties, Channel channel);
    Object rpc(String exchangeName, String key, Object message, Class clazz, Channel channel) throws IOException;
}