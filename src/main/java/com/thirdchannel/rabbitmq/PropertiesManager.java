package com.thirdchannel.rabbitmq;

import com.thirdchannel.rabbitmq.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Steve Pember
 */
class PropertiesManager {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public RabbitMQConfig load() throws FileNotFoundException, URISyntaxException {
        URL url = PropertiesManager.class.getResource("lago.yaml");

        File file = new File(url.toURI());
        InputStream input = new FileInputStream(file);
        try {
            Yaml yaml = new Yaml(new Constructor(RabbitMQConfig.class));
            RabbitMQConfig data = (RabbitMQConfig)yaml.load(input);
            log.info("RabbitMQ configuration loaded");
            return data;
        } catch(YAMLException ye) {
            log.error("Could not load configuration: ", ye);
            return null;
        }

    }
}
