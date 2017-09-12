package com.ants.common.config;

import com.ants.common.model.ElasticSearchInfo;
import com.ants.common.util.StringUtil;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by thangpham on 11/09/2017.
 */
public class ElasticSearchConfiguration {

    private Configuration config = null;
    private static ElasticSearchConfiguration _instance;

    private ConcurrentMap<String, ElasticSearchInfo> mapConfig;
    private int totalClient;

    public static ElasticSearchConfiguration load() throws ConfigurationException {
        if (null == _instance) {
            synchronized (ElasticSearchConfiguration.class) {
                _instance = new ElasticSearchConfiguration();
            }
        }
        return _instance;
    }

    public ElasticSearchConfiguration() throws ConfigurationException {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties()
                        .setFileName(BaseConfiguration.getElasticSearchConfigFile())
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        config = builder.getConfiguration();
    }

    public int getTotalClient() {
        return totalClient;
    }

    public ElasticSearchInfo getEsClusterConfig(String clusterName) {
        ElasticSearchInfo get = mapConfig.get(clusterName);
        if (get == null) {
            loadEsCluster(clusterName);
        }
        return mapConfig.get(clusterName);
    }

    private void loadEsCluster(String clusterName) {
        List<String> adxES = config.getList(String.class, "data.es." + clusterName + ".address");
        String name = config.getString("data.es." + clusterName + ".name");
        List<ElasticSearchInfo.HostPort> listNodes = new ArrayList();
        ElasticSearchInfo info = new ElasticSearchInfo(name, listNodes);
        mapConfig.put(clusterName, info);
        for (String node : adxES) {
            String[] split = node.split(":");
            listNodes.add(new ElasticSearchInfo.HostPort(split[0], StringUtil.safeParseInt(split[1])));
        }
    }

}
