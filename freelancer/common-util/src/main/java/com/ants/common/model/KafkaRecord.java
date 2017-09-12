package com.ants.common.model;

/**
 * Created by thangpham on 12/09/2017.
 */
public class KafkaRecord {

    private long firstFailed;
    private int partition;
    private String key;
    private String value;

    public KafkaRecord(long firstFailed, int partition, String key, String value) {
        this.firstFailed = firstFailed;
        this.partition = partition;
        this.key = key;
        this.value = value;
    }

    public KafkaRecord(int partition, String key, String value) {
        this.partition = partition;
        this.key = key;
        this.value = value;
    }

    public long getFirstFailed() {
        return firstFailed;
    }

    public void setFirstFailed(long firstFailed) {
        this.firstFailed = firstFailed;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}