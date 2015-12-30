package com.thirdchannel.rabbitmq;

import com.thirdchannel.rabbitmq.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Steve Pember
 */
class PropertiesManager {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public RabbitMQConfig load() throws FileNotFoundException {

        InputStream input = PropertiesManager.class.getResourceAsStream("/lago.yaml");
        if (input == null) {
            throw new FileNotFoundException("Could not find lago.yaml on the classpath");
        }

        try {
            Yaml yaml = new Yaml(new Constructor(RabbitMQConfig.class));
            RabbitMQConfig data = (RabbitMQConfig)yaml.load(input);
            log.info("RabbitMQ configuration loaded");
            input.close();
            return data;
        } catch(YAMLException | IOException ye) {
            log.error("Could not load configuration: ", ye);
            return null;
        }
    }
}
