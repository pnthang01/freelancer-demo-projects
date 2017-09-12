package com.ants.common.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by thangpham on 11/09/2017.
 */
public class DruidConfiguration {

    private Configuration config = null;
    private static DruidConfiguration _instance = null;
    private AtomicInteger count = null;
    private List<String> queryUriList = null;

    public static DruidConfiguration load() throws ConfigurationException {
        if (null == _instance) {
            synchronized (DruidConfiguration.class) {
                _instance = new DruidConfiguration();
            }
        }
        return _instance;
    }

    public DruidConfiguration() throws ConfigurationException {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties()
                        .setFileName(BaseConfiguration.getDruidConfigFile())
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        config = builder.getConfiguration();
        count = new AtomicInteger();
        queryUriList = getDruidQueryURIs();
    }

    public String getDruidQueryURI() {
        List<String> list = getDruidQueryURIs();
        return list.get(count.incrementAndGet() % list.size());
    }

    public List<String> getDruidQueryURIs() {
        if(null == queryUriList) {
            queryUriList = config.getList(String.class, "druid.uri.query");
        }
        return queryUriList;
    }

    public String getDruidTaskURI() {
        return config.getString("druid.uri.task");
    }

    public String getDruidMetadataURI() {
        return config.getString("druid.uri.metadata");
    }

    public String getDruidDatasourceURI() {
        return config.getString("druid.uri.datasource");
    }

    public String getDruidRuleURI() {
        return config.getString("druid.uri.rule");
    }
}
