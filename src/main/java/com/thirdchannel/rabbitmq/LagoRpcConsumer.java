package com.thirdchannel.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.thirdchannel.rabbitmq.interfaces.RpcConsumer;

import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.Map;

/**
 * @author Steve Pember
 */
public abstract class LagoRpcConsumer<M, R> extends LagoEventConsumer<M> implements RpcConsumer<M, R> {
    private Class<R> responseClass;

    @Override
    public boolean handleMessage(M data, RabbitMQDeliveryDetails rabbitMQDeliveryDetails) {
        RpcStopWatch stopWatch = null;
        if (getConfig().isLogTime()) {stopWatch = new RpcStopWatch("RPC proccessed").start();}

        R response = handleRPC(data, rabbitMQDeliveryDetails);
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .correlationId(rabbitMQDeliveryDetails.getBasicProperties().getCorrelationId())
                .build();
        String replyTo = rabbitMQDeliveryDetails.getBasicProperties().getReplyTo();
        log.debug("Responding to RPC Query one queue " + replyTo);
        // publish with the replyTo as the topic / Routing key on the same channel that this consumer is listening on
        // if we do not specify, the service will use the main channel, which may not be what we want
        getLago().publish(rabbitMQDeliveryDetails.getEnvelope().getExchange(), replyTo, response, replyProps);

        if (getConfig().isLogTime() && stopWatch != null) {
            stopWatch.stopAndPublish(rabbitMQDeliveryDetails);
        }
        return true;

    }

    @Override
    public Class<R> getResponseClass() {
        if (responseClass == null) {
            setResponseClass();
        }
        return responseClass;
    }

    @SuppressWarnings("unchecked")
    private void setResponseClass() {
        responseClass = ((Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1]);
        log.trace("Set generic type of " + responseClass.getSimpleName());
    }
}
