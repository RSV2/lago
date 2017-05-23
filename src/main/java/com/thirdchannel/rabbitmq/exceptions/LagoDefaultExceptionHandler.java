package com.thirdchannel.rabbitmq.exceptions;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.impl.DefaultExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Steve Pember
 */
public class LagoDefaultExceptionHandler extends DefaultExceptionHandler {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void handleChannelKiller(Channel channel, Throwable exception, String what) {
        log.error("Consumer Exception observed due to {}:", what, exception);
    }
}
