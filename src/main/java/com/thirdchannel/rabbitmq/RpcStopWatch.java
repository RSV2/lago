package com.thirdchannel.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * Class used by consumers and by the root Lago object to track.
 *
 * Currently just logs to a file, but if we need alternatives (say, rabbitMQ), turn this into an interface
 *
 * @author Steve Pember
 */
class RpcStopWatch {
    public static String TRACE_ID = "traceId";
    private static Logger log = LoggerFactory.getLogger(RpcStopWatch.class);

    private long startTime = 0;
    private long elapsedTime = 0;

    private String prefix = "RPC ";

    public RpcStopWatch() {
    }

    public RpcStopWatch(String prefix) {
        this.prefix = prefix;
    }

    public RpcStopWatch start() {
        startTime = new Date().getTime();
        return this;
    }

    public RpcStopWatch stopAndPublish(RabbitMQDeliveryDetails rabbitMQDeliveryDetails) {
        elapsedTime = new Date().getTime() - startTime;
        logTime(rabbitMQDeliveryDetails);
        return this;
    }

    private void logTime(RabbitMQDeliveryDetails rabbitMQDeliveryDetails) {
        StringBuilder builder = new StringBuilder();
        String spanId = "unknown";
        Map<String, Object> headers = rabbitMQDeliveryDetails.getBasicProperties().getHeaders();
        if (headers != null && headers.containsKey(TRACE_ID)) {
            spanId = headers.get(TRACE_ID).toString();
        }
        builder.append(prefix).append(" complete: [")
                .append(spanId)
                .append("] [")
                .append(rabbitMQDeliveryDetails.getBasicProperties().getCorrelationId())
                .append("] [")
                .append(rabbitMQDeliveryDetails.getEnvelope().getRoutingKey())
                .append("] [")
                .append(new Date().getTime() - startTime)
                .append("ms]");
        log.info(builder.toString());
    }
}
