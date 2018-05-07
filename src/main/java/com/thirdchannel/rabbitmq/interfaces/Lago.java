package com.thirdchannel.rabbitmq.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.thirdchannel.rabbitmq.exceptions.RPCTimeoutException;
import com.thirdchannel.rabbitmq.exceptions.RabbitMQSetupException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Steve Pember
 */
public interface Lago {

    void close();

    Connection getConnection();
    Connection connect() throws RabbitMQSetupException;
    Connection connect(String url) throws RabbitMQSetupException;
    Connection connect(String userName, String password, String virtualHost, String host, int port) throws RabbitMQSetupException;
    Connection connect(ConnectionFactory factory) throws RabbitMQSetupException;
    Channel createChannel() throws RabbitMQSetupException;
    Channel getChannel();

    ObjectMapper getObjectMapper();
    void setObjectMapper(ObjectMapper objectMapper);

    void setExceptionHandler(ExceptionHandler handler);
    List<EventConsumer> getRegisteredConsumers();
    void registerConsumer(EventConsumer consumer) throws RabbitMQSetupException;

    void publish(String exchangeName, String key, Object message, AMQP.BasicProperties properties) throws IOException;
    void publish(String exchangeName, String key, Object message, AMQP.BasicProperties properties, Channel channel) throws IOException;

    @Deprecated
    Object rpc(
        String exchangeName,
        String key,
        Object message,
        Class clazz,
        Channel channel
    ) throws IOException, RPCTimeoutException;
    @Deprecated
        Object rpc(
        String exchangeName,
        String key,
        Object message,
        Class<? extends Collection> collectionClazz,
        Class clazz,
        Channel channel
    ) throws IOException, RPCTimeoutException;
    @Deprecated
    Object rpc(
        String exchangeName,
        String key,
        Object message,
        Class<? extends Collection> collectionClazz,
        Class clazz,
        Channel channel,
        String traceId,
        Integer rpcTimeout
    ) throws IOException, RPCTimeoutException;


    Optional<Object> optionalRpc(
            String exchangeName,
            String key,
            Object message,
            Class clazz,
            Channel channel
    ) throws IOException, RPCTimeoutException;
    Optional<Object> optionalRpc(
            String exchangeName,
            String key,
            Object message,
            Class<? extends Collection> collectionClazz,
            Class clazz,
            Channel channel
    ) throws IOException, RPCTimeoutException;
    Optional<Object> optionalRpc(
            String exchangeName,
            String key,
            Object message,
            Class<? extends Collection> collectionClazz,
            Class clazz,
            Channel channel,
            String traceId,
            Integer rpcTimeout
    ) throws IOException, RPCTimeoutException;
}
