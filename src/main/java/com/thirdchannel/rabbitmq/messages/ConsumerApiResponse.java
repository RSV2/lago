package com.thirdchannel.rabbitmq.messages;

import java.util.ArrayList;
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
