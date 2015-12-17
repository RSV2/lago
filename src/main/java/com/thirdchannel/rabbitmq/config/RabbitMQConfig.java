package com.thirdchannel.rabbitmq.config;

import com.thirdchannel.rabbitmq.interfaces.EventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Pember
 */
public class RabbitMQConfig {
    private static Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    private static final String RABBITMQ_URL_KEY = "RABBITMQ_URL";

    private String connectionUrl;

    private int rpcTimeout = 5000; // milliseconds
    private int connectionTimeout = 10000; //milliseconds
    private int heartbeatInterval = 5; // seconds, wtf
    private boolean automaticRecoveryEnabled = true;
    private boolean topologyRecoveryEnabled = true;
    private boolean logRpcTime = true; // if true, will log the time an rpc call takes

    // connection information

    private String virtualHost = "/";
    private String username;
    private String password;
    private String host;
    private int port = 5672;

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    private List<ExchangeConfig> exchanges = new ArrayList<ExchangeConfig>();

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public List<ExchangeConfig> getExchanges() {
        return exchanges;
    }

    public void setExchanges(List<ExchangeConfig> exchanges) {
        this.exchanges = exchanges;
    }

    public int getRpcTimeout() {
        return rpcTimeout;
    }

    public void setRpcTimeout(int rpcTimeout) {
        this.rpcTimeout = rpcTimeout;
    }

    public boolean isAutomaticRecoveryEnabled() {
        return automaticRecoveryEnabled;
    }

    public void setAutomaticRecoveryEnabled(boolean automaticRecoveryEnabled) {
        this.automaticRecoveryEnabled = automaticRecoveryEnabled;
    }

    public boolean isTopologyRecoveryEnabled() {
        return topologyRecoveryEnabled;
    }

    public void setTopologyRecoveryEnabled(boolean topologyRecoveryEnabled) {
        this.topologyRecoveryEnabled = topologyRecoveryEnabled;
    }

    public boolean isLogRpcTime() {
        return logRpcTime;
    }

    public void setLogRpcTime(boolean logRpcTime) {
        this.logRpcTime = logRpcTime;
    }

    public boolean hasConnectionConfig() {
        return username != null && !username.isEmpty() && password != null && !password.isEmpty() && host != null
                && !host.isEmpty();
    }

    /**
     * Looks up a connection url in environment and system properties
     *
     * @return String connection url
     */
    public String getConnectionEnvironmentUrl() {
        String url = System.getenv(RABBITMQ_URL_KEY);
        if (url == null || (url != null && url.isEmpty())) {
            url = System.getProperty(RABBITMQ_URL_KEY);
        }
        log.debug("Located connection url " + url);

        return url == null ? "" : url;
    }

    public QueueConsumerConfig findQueueConfig(EventConsumer consumer) {
        String factoryName = consumer.getClass().getSimpleName();
        QueueConsumerConfig foundQueueConsumerConfig = null;
        log.debug("Looking for Consumer with name " + consumer.getClass().getSimpleName());
        for (ExchangeConfig exchangeConfig : this.getExchanges()) {
            for (QueueConsumerConfig queueConsumerConfig : exchangeConfig.getQueues()) {
                if (factoryName.toLowerCase().startsWith(queueConsumerConfig.getConsumer().toLowerCase())) {
                    log.debug("Configuration located for Consumer: " + factoryName);
                    foundQueueConsumerConfig = queueConsumerConfig;
                    // set the exchange name on the lower level config object to avoid having to find a way to return two objects
                    foundQueueConsumerConfig.setExchangeName(exchangeConfig.getName());
                }
            }
        }

        if (foundQueueConsumerConfig == null) {
            log.warn("Could not find matching configuration for factory: " + factoryName);
            foundQueueConsumerConfig = new QueueConsumerConfig();
            foundQueueConsumerConfig.setCount(0); // ensure it won't run
        }
        return foundQueueConsumerConfig;
    }


}
