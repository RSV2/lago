package com.thirdchannel.rabbitmq.messages;

import com.thirdchannel.rabbitmq.consumers.EventConsumer;
import com.thirdchannel.rabbitmq.consumers.LagoRpcConsumer;
import com.thirdchannel.rabbitmq.consumers.RpcConsumer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Steve Pember
 */
public class ConsumerApiResponse {

    String exchange;
    String key;

    List<Map<String, String>> in = new ArrayList<Map<String,String>>();
    List<Map<String, String>> out = new ArrayList<Map<String,String>>();

    ConsumerApiResponse() {
    }

    ConsumerApiResponse(EventConsumer consumer) {
        setExchange(consumer.getConfig().getExchangeName());
        setKey(consumer.getConfig().getKey());

        Class m = consumer.getMessageClass();
        setIn(exportFields(m));


        if (consumer instanceof RpcConsumer) {
            //Class c = ((LagoRpcConsumer) consumer).getResponseClass();
            setOut(exportFields(((LagoRpcConsumer) consumer).getResponseClass()));
        }
    }

    private List<Map<String, String>> exportFields(Class c) {
        List<Map<String, String>> fields = new ArrayList<Map<String, String>>();

        for (Field field : c.getDeclaredFields()) {
            Map<String, String> description = new HashMap<String, String>();
            description.put("name", field.getName());
            description.put("type", field.getType().getSimpleName());
            fields.add(description);
        }
        return fields;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Map<String, String>> getIn() {
        return in;
    }

    public void setIn(List<Map<String, String>> in) {
        this.in = in;
    }

    public List<Map<String, String>> getOut() {
        return out;
    }

    public void setOut(List<Map<String, String>> out) {
        this.out = out;
    }
}
