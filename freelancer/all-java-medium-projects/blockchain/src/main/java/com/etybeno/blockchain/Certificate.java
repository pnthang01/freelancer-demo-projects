package com.etybeno.blockchain;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by thangpham on 30/01/2018.
 */
public class Certificate {

    private String redeemAddress;
    private String arbitraryData;
    private int maxNonce;
    private String authorityName;
    private int blockNum;
    private String prevBlockHash;
    private long signatureIndex;
    private String signatureData;
    private final Map<String, String> authorities;

    public Certificate(String redeemAddress, String arbitraryData, int maxNonce, String authorityName, int blockNum,
                       String prevBlockHash, long signatureIndex, String signatureData) {
        this.redeemAddress = redeemAddress;
        this.arbitraryData = arbitraryData;
        this.maxNonce = maxNonce;
        this.authorityName = authorityName;
        this.blockNum = blockNum;
        this.prevBlockHash = prevBlockHash;
        this.signatureIndex = signatureIndex;
        this.signatureData = signatureData;
        this.authorities = new HashMap<>();

    }
}
