package com.ants.common.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by thangpham on 29/06/2017.
 */
public class ElasticSearchInfo {

    private String clusterName;
    private List<HostPort> nodeList;

    public ElasticSearchInfo(String clusterName, List<HostPort> nodeList) {
        this.clusterName = clusterName;
        this.nodeList = nodeList;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<HostPort> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<HostPort> nodeList) {
        this.nodeList = nodeList;
    }

    public static class HostPort implements Serializable {

        private String host;
        private int port;

        public HostPort(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

    }

}