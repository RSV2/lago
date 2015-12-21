package com.thirdchannel.rabbitmq.messages;

import com.thirdchannel.rabbitmq.interfaces.EventConsumer;
import com.thirdchannel.rabbitmq.LagoRpcConsumer;
import com.thirdchannel.rabbitmq.interfaces.RpcConsumer;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Steve Pember
 */
public class ConsumerApiResponse {

    String exchange;
    List<String> keys;

    Map<String, String> in = new HashMap<String,String>();
    Map<String, String> out = new HashMap<String,String>();

    ConsumerApiResponse() {
    }

    ConsumerApiResponse(EventConsumer consumer) {
        setExchange(consumer.getConfig().getExchangeName());
        setKeys(consumer.getConfig().getKeys());

        Class m = consumer.getMessageClass();
        setIn(exportFields(m));


        if (consumer instanceof RpcConsumer) {
            setOut(exportFields(((LagoRpcConsumer) consumer).getResponseClass()));
        }
    }

    private Map<String, String> exportFields(Class c) {
        Map<String, String> fields = new HashMap<String, String>();

        for (Method method: c.getDeclaredMethods()) {
            BeanInfo info = null;
            try {
                info = Introspector.getBeanInfo(c);

                PropertyDescriptor[] props = info.getPropertyDescriptors();
                for (PropertyDescriptor pd : props) {
                    if (!method.isSynthetic() && method.equals(pd.getReadMethod())) {
                        fields.put(pd.getDisplayName(), pd.getPropertyType().getSimpleName());
                    }
                }
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
        }
        return fields;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public Map<String, String> getIn() {
        return in;
    }

    public void setIn(Map<String, String> in) {
        this.in = in;
    }

    public Map<String, String> getOut() {
        return out;
    }

    public void setOut(Map<String, String> out) {
        this.out = out;
    }
}
