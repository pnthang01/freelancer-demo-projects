package com.ants.common.config;

import com.ants.common.model.RedisInfo;
import com.ants.common.util.StringUtil;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by thangpham on 08/09/2017.
 */
public class RedisInfoConfiguration implements Serializable{

    private Configuration config = null;
    private ConcurrentMap<String, List<RedisInfo>> redisMap;
    private static RedisInfoConfiguration _instance = null;

    public static RedisInfoConfiguration load() throws ConfigurationException {
        if (null == _instance) {
            synchronized (RedisInfoConfiguration.class) {
                _instance = new RedisInfoConfiguration();
            }
        }
        return _instance;
    }

    public RedisInfoConfiguration() throws ConfigurationException {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties()
                        .setFileName(BaseConfiguration.getRedisConfigFile())
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        config = builder.getConfiguration();
        redisMap = new ConcurrentHashMap();
    }

    public RedisInfo getSingleRedisInfo(String redisName) throws ConfigurationException, IOException {
        return getMultiRedisInfo(redisName).get(0);
    }

    public List<RedisInfo> getMultiRedisInfo(String redisName) throws ConfigurationException, IOException {
        List<RedisInfo> list = redisMap.get(redisName);
        if (null == list) {
            list = new ArrayList();
            List<String> hostPorts = config.getList(String.class, "data.redis." + redisName);
            for (int i = 0; i < hostPorts.size(); ++i) {
                String[] hostPort = hostPorts.get(i).split(":");
                RedisInfo redisInfo = new RedisInfo(hostPort[0], StringUtil.safeParseInt(hostPort[1]));
                list.add(redisInfo);
            }
            redisMap.put(redisName, list);
        }
        return list;
    }

}
