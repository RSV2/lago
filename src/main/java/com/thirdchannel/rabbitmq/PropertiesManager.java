package com.thirdchannel.rabbitmq;

import com.thirdchannel.rabbitmq.config.RabbitMQConfig;
import com.thirdchannel.rabbitmq.exceptions.LagoConfigLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Steve Pember
 */
class PropertiesManager {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public RabbitMQConfig load() throws LagoConfigLoadException {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("lago.yaml")){
            if (input == null) {
                throw new LagoConfigLoadException("Could not find lago.yaml on the classpath");
            }
            Yaml yaml = new Yaml(new Constructor(RabbitMQConfig.class));
            RabbitMQConfig data = (RabbitMQConfig) yaml.load(input);
            if(data == null ) {
                throw new LagoConfigLoadException("Unable to load RabbitMQ config from lago.yaml");
            }
            log.debug("RabbitMQ configuration loaded");
            return data;
        } catch (IOException e) {
            throw new LagoConfigLoadException("Could not close input stream for lago.yaml");
        }
    }
}
