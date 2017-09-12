package com.ants.common.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;

/**
 * Created by thangpham on 08/09/2017.
 */
public class RedisConnPoolConfiguration {

    private Configuration config = null;
    private static RedisConnPoolConfiguration _instance = null;

    public static RedisConnPoolConfiguration load() throws ConfigurationException, IOException {
        if (null == _instance) {
            synchronized (RedisConnPoolConfiguration.class) {
                _instance = new RedisConnPoolConfiguration();
            }
        }
        return _instance;
    }

    public RedisConnPoolConfiguration() throws ConfigurationException, IOException {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties()
                        .setFileName(BaseConfiguration.getRedisConfigFile())
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        config = builder.getConfiguration();
    }

    public JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(config.getInteger("data.redis.connect.maxActive", 20));
        poolConfig.setMaxIdle(config.getInteger("data.redis.connect.maxIdle", 10));
        poolConfig.setMinIdle(config.getInteger("data.redis.connect.minIdle", 1));
        poolConfig.setMaxWaitMillis(config.getLong("data.redis.connect.maxWait", 3000));
        poolConfig.setNumTestsPerEvictionRun(config.getInt("data.redis.connect.numTestsPerEvictionRun", 10));
        poolConfig.setTestOnBorrow(config.getBoolean("data.redis.connect.testOnBorrow", true));
        poolConfig.setTestOnReturn(config.getBoolean("data.redis.connect.testOnReturn", true));
        poolConfig.setTestWhileIdle(config.getBoolean("data.redis.connect.testWhileIdle", true));
        poolConfig.setTimeBetweenEvictionRunsMillis(config.getLong("data.redis.connect.timeBetweenEvictionRunsMillis", 60000));
        return poolConfig;
    }
}