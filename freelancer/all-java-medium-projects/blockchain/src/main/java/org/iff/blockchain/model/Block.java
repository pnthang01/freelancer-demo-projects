package org.iff.blockchain.model;

/**
 * Created by thangpham on 13/11/2017.
 */
public class Block {

    private long index;
    private long timestamp;
    private String data;
    private String hash;
    private String previousHash;

    public long getIndex() {
        return index;
    }

    public Block setIndex(long index) {
        this.index = index;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Block setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getData() {
        return data;
    }

    public Block setData(String data) {
        this.data = data;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public Block setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public Block setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
        return this;
    }
}
