package com.ants.common.config;

import com.ants.common.util.StringUtil;

/**
 * Created by thangpham on 11/09/2017.
 */
public class BaseConfiguration {

    private static String REDIS_CONFIG_FILE = "redis-info-configs.properties";
    private static String DRUID_CONFIG_FILE = "druid-uri-configs.properties";
    private static String DATABASE_CONFIG_FILE = "database-info-configs.properties";
    private static String HADOOP_CONFIG_FILE = "hadoop-connection-configs.properties";
    private static String SNAPPYDATA_CONFIG_FILE = "snappy-data-uri-configs.properties";
    private static String ELASTICSEARCH_CONFIG_FILE = "elasticsearch-configs.properties";
    private static String KAFKA_PRODUCERS_CONFIGS_FILE = "kafka-producers-configs.properties";
    private static String baseConfig = "config/";

    public static void setBaseConfig(String targetConfDir) throws IllegalArgumentException {
        if (StringUtil.isNullOrEmpty(targetConfDir)) {
            throw new IllegalArgumentException("The configuration does not have base config. Could not lookup configurations.");
        }
        BaseConfiguration.baseConfig = targetConfDir;
    }

    public static String getKafkaProducersConfigFile() {
        return baseConfig + KAFKA_PRODUCERS_CONFIGS_FILE;
    }

    public static void setKafkaProducersConfigsFile(String fileName) {
        KAFKA_PRODUCERS_CONFIGS_FILE = fileName;
    }

    public static void setElasticsearchConfigFile(String fileName) {
        ELASTICSEARCH_CONFIG_FILE = fileName;
    }

    public static String getElasticSearchConfigFile() {
        return baseConfig + ELASTICSEARCH_CONFIG_FILE;
    }

    public static void setSnappydataConfigFile(String fileName) {
        SNAPPYDATA_CONFIG_FILE = fileName;
    }

    public static String getSnappyDataConfigFile() {
        return baseConfig + SNAPPYDATA_CONFIG_FILE;
    }

    public static void setDatabaseConfigFile(String fileName) {
        DATABASE_CONFIG_FILE = fileName;
    }

    public static String getDatabaseConfigFile() {
        return baseConfig + DATABASE_CONFIG_FILE;
    }

    public static void setRedisConfigFile(String fileName) {
        REDIS_CONFIG_FILE = fileName;
    }

    public static String getRedisConfigFile() {
        return baseConfig + REDIS_CONFIG_FILE;
    }

    public static void setDruidConfigFile(String fileName) {
        DRUID_CONFIG_FILE = fileName;
    }

    public static String getDruidConfigFile() {
        return baseConfig + DRUID_CONFIG_FILE;
    }

    public static void setHadoopConfigFile(String fileName) {
        HADOOP_CONFIG_FILE = fileName;
    }

    public static String getHadoopConfigFile() {
        return baseConfig + HADOOP_CONFIG_FILE;
    }

}
