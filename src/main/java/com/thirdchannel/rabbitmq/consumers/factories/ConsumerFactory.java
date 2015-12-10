package com.thirdchannel.rabbitmq.consumers.factories;

import com.thirdchannel.rabbitmq.config.QueueConfig;
import com.thirdchannel.rabbitmq.consumers.EventConsumer;

/**
 * Responsible for generating a set number of consumers
 *
 * @author Steve Pember
 */
public interface ConsumerFactory<C extends EventConsumer> {

    C produceConsumer();

    // this could be trimmed down a bit, there's no major reason why the queue information needs to exist here

    String getQueueName();
    void setQueueName(String name);

    String getExchangeName();
    void setExchangeName(String exchangeName);

    void setCount(int count);
    int getCount();

    void setDurable(boolean durable);
    boolean isDurable();

    void setAutoDelete(boolean autoDelete);
    boolean isAutoDelete();

    void setKey(String key);
    String getKey();

    void applyConfig(QueueConfig queueConfig);

}
