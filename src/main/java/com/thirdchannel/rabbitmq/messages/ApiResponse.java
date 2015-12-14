package com.thirdchannel.rabbitmq.messages;

import com.thirdchannel.rabbitmq.consumers.EventConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Pember
 */
public class ApiResponse {

    private List<ConsumerApiResponse> consumers = new ArrayList<ConsumerApiResponse>();

    public List<ConsumerApiResponse> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<ConsumerApiResponse> consumers) {
        this.consumers = consumers;
    }

    public void parseConsumer(EventConsumer consumer) {

    }
}
