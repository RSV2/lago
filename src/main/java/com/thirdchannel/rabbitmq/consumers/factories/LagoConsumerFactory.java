package com.thirdchannel.rabbitmq.consumers.factories;

import com.thirdchannel.rabbitmq.config.QueueConfig;
import com.thirdchannel.rabbitmq.consumers.EventConsumer;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Steve Pember
 */
abstract public class LagoConsumerFactory<C extends EventConsumer> implements ConsumerFactory<C>{

    private int count;
    private boolean durable;
    private boolean autoDelete;
    private boolean autoAck;

    private String key; // or topic
    private String queueName;
    private String exchangeName;

    public boolean isAutoAck() {
        return autoAck;
    }

    public void setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }


    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    public void setQueueName(String name) {
        queueName = name;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void applyConfig(QueueConfig queueConfig) {
        setKey(queueConfig.getKey());
        setAutoDelete(queueConfig.isAutoDelete());
        setDurable(queueConfig.isDurable());
        setCount(queueConfig.getCount());
        setAutoAck(queueConfig.isAutoAck());
        setExchangeName(queueConfig.getExchangeName());
        setQueueName(queueConfig.getName());
    }

    @Override
    public boolean isConfigured() {
        return !StringUtils.isEmpty(getQueueName()) && !StringUtils.isEmpty(getExchangeName()) && !StringUtils.isEmpty(getKey());
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    @Override
    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public boolean isDurable() {
        return durable;
    }

    @Override
    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }




}
