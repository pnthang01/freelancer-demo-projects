package com.ants.common.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by thangpham on 08/09/2017.
 */
public class HadoopConnConfiguration {

    private static HadoopConnConfiguration _instance = null;
    private Configuration config = null;
    private ConcurrentMap<String, org.apache.hadoop.conf.Configuration> hadoopConfigMap;

    public static final String HADOOP_DEFAULT_CLUSTER = "default";

    public static HadoopConnConfiguration load() throws ConfigurationException {
        if (null == _instance) {
            synchronized (HadoopConnConfiguration.class) {
                _instance = new HadoopConnConfiguration();
            }
        }
        return _instance;
    }

    public HadoopConnConfiguration() throws ConfigurationException {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties()
                        .setFileName(BaseConfiguration.getHadoopConfigFile())
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        config = builder.getConfiguration();
        hadoopConfigMap = new ConcurrentHashMap();
    }

    private synchronized org.apache.hadoop.conf.Configuration loadHadoopConfig(String name, Integer replication) {
        org.apache.hadoop.conf.Configuration hadoopConfig = new org.apache.hadoop.conf.Configuration();
        hadoopConfig.set("fs.default.name", "hdfs://" + config.getString("data.hadoop.hdfs.address"));
        hadoopConfig.set("dfs.replication", replication.toString());
        org.apache.hadoop.conf.Configuration tmp = hadoopConfigMap.putIfAbsent(name, hadoopConfig);
        return tmp == null ? hadoopConfig : tmp;
    }

    public org.apache.hadoop.conf.Configuration getHadoopConfig(String name, Integer replication) {
        org.apache.hadoop.conf.Configuration hadoopConfig = hadoopConfigMap.get(name);
        if (null == hadoopConfig) {
            hadoopConfig = loadHadoopConfig(name, replication);
        }
        return hadoopConfig;
    }

    public org.apache.hadoop.conf.Configuration getHadoopConfig(String name) {
        org.apache.hadoop.conf.Configuration hadoopConfig = hadoopConfigMap.get(name);
        if (null == hadoopConfig) {
            hadoopConfig = loadHadoopConfig(name, 2);
        }
        return hadoopConfig;
    }
}
