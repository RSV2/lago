package com.thirdchannel.rabbitmq.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Pember
 */
public class QueueConsumerConfig {

    private String name = "";
    private int count = 1;
    private List<String> keys = new ArrayList<>();
    private String exchangeName = "";
    private boolean durable = true;
    private boolean autoDelete = false;
    private boolean autoAck = false;
    private boolean logTime = true; // if true, will log processing time for each request on this consumer

    public boolean isBackwardsCompatible() {
        return backwardsCompatible;
    }

    public void setBackwardsCompatible(boolean backwardsCompatible) {
        this.backwardsCompatible = backwardsCompatible;
    }

    private boolean backwardsCompatible = false;
    private int prefetch = 1;

    private String consumer;

    public boolean isAutoAck() {
        return autoAck;
    }

    public void setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public boolean isLogTime() {
        return logTime;
    }

    public void setLogTime(boolean logTime) {
        this.logTime = logTime;
    }

    public int getPrefetch() {
        return prefetch;
    }

    public void setPrefetch(int prefetch) {
        this.prefetch = prefetch;
    }
}
