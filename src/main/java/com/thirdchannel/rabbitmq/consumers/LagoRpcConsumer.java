package com.thirdchannel.rabbitmq.consumers;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.thirdchannel.rabbitmq.RabbitMQDeliveryDetails;

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
        long start = 0;
        if (getConfig().isLogTime()) {start = new Date().getTime();}

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
        log.info("Log time? " + getConfig().isLogTime());
        if (getConfig().isLogTime()) {
            logTime(start, rabbitMQDeliveryDetails);
        }
        return true;

    }

    private void logTime(long start, RabbitMQDeliveryDetails rabbitMQDeliveryDetails) {
        StringBuilder builder = new StringBuilder();
        String spanId = "unknown";
        Map<String, Object> headers = rabbitMQDeliveryDetails.getBasicProperties().getHeaders();
        if (headers != null && headers.containsKey("spanId")) {
            spanId = headers.get("spanId").toString();
        }
        builder.append("RPC processing complete: [")
                .append(spanId)
                .append("] [")
                .append(rabbitMQDeliveryDetails.getBasicProperties().getCorrelationId())
                .append("] [")
                .append(rabbitMQDeliveryDetails.getEnvelope().getRoutingKey())
                .append("] [")
                .append(new Date().getTime() - start)
                .append("ms]");
        log.info(builder.toString());
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
