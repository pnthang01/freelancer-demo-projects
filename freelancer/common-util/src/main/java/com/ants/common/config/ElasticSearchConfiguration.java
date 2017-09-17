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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by thangpham on 11/09/2017.
 */
public class ElasticSearchConfiguration {

    static final Logger LOGGER = LogManager.getLogger(ElasticSearchConfiguration.class);
    private Configuration config = null;
    private static ElasticSearchConfiguration _instance;

    private ConcurrentMap<String, ElasticSearchInfo> mapConfig = new ConcurrentHashMap<>();
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

    public String getIndex(String clusterName) {
        return config.getString("data.es." + clusterName + ".index");
    }

    public String getType(String clusterName) {
        return config.getString("data.es." + clusterName + ".type");
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
        String nodeName = config.getString("data.es." + clusterName + ".node.name");
//        String name = config.getString("data.es." + clusterName + ".name");
        List<ElasticSearchInfo.HostPort> listNodes = new ArrayList();
        ElasticSearchInfo info = new ElasticSearchInfo(nodeName, clusterName, listNodes);
        for (String node : adxES) {
            String[] split = node.split(":");
            listNodes.add(new ElasticSearchInfo.HostPort(split[0], StringUtil.safeParseInt(split[1])));
        }
        mapConfig.put(clusterName, info);
    }

    public TransportClient getClient(String clusterName) throws UnknownHostException, ConfigurationException {
        TransportClient client = null;
        ElasticSearchInfo info = ElasticSearchConfiguration.load().getEsClusterConfig(clusterName);
        Settings settings = Settings.builder()
                .put("node.name", info.getNodeName())
                .put("cluster.name", clusterName).build();
        List<ElasticSearchInfo.HostPort> nodeList = info.getNodeList();
        try {
            client = new PreBuiltTransportClient(settings);
            for(ElasticSearchInfo.HostPort hostPort : nodeList) {
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostPort.getHost()), hostPort.getPort()));
            }
        } catch (Exception e) {
            LOGGER.error("Error when create TransportClient ", e);
        }
        return client;
    }

}
