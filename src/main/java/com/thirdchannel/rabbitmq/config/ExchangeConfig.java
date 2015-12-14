package com.thirdchannel.rabbitmq.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Pember
 */
public class ExchangeConfig {

    private String name;
    private String type = "direct";
    private boolean autoDelete = false;
    private boolean durable = true;
    private List<QueueConsumerConfig> queues = new ArrayList<QueueConsumerConfig>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<QueueConsumerConfig> getQueues() {
        return queues;
    }

    public void setQueues(List<QueueConsumerConfig> queues) {
        this.queues = queues;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }


    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }
}
