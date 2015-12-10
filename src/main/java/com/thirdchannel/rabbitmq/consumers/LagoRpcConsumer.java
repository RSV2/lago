package com.thirdchannel.rabbitmq.consumers;

import com.rabbitmq.client.AMQP;
import com.thirdchannel.rabbitmq.RabbitMQDeliveryDetails;

/**
 * @author Steve Pember
 */
public abstract class LagoRpcConsumer<M, R> extends LagoEventConsumer<M> implements RpcConsumer<M, R> {
    @Override
    public boolean handleMessage(M data, RabbitMQDeliveryDetails rabbitMQDeliveryDetails) {


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
        return true;

    }
}
