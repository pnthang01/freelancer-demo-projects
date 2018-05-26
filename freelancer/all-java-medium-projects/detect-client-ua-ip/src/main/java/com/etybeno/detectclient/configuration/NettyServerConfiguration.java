package com.etybeno.detectclient.configuration;

import com.etybeno.common.config.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;

/**
 * Created by thangpham on 21/05/2018.
 */
public class NettyServerConfiguration {

    private static NettyServerConfiguration _instance;

    public synchronized static NettyServerConfiguration _load() throws ConfigurationException, IOException {
        if (null == _instance) _instance = new NettyServerConfiguration();
        return _instance;
    }

    private Configuration config = null;

    public NettyServerConfiguration() throws ConfigurationException, IOException {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties()
                        .setFileName(BaseConfiguration.getNettyServerConfigFile())
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        config = builder.getConfiguration();
    }

    public Configuration getConfig() {
        return config;
    }

    public String getServer() { return config.getString("server.host"); }

    public int getPort() { return config.getInt("server.port");}

}
