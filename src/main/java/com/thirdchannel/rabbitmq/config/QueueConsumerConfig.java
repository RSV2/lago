package com.thirdchannel.rabbitmq.config;

/**
 * @author Steve Pember
 */
public class QueueConsumerConfig {

    private String name = "";
    private int count = 1;
    private String key = "";
    private String exchangeName = "";
    private boolean durable = false;
    private boolean autoDelete = false;
    private boolean autoAck = true;
    private boolean logTime = true; // if true, will log processing time for each request on this consumer

    private String factory;

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
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
}
