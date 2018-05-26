package com.etybeno.blockchain;


import java.security.cert.Certificate;
import java.security.MessageDigest;
import java.util.List;

/**
 * Created by thangpham on 30/01/2018.
 */
public class Block {

    private long timestamp;
    private int blockNum;
    private String previousBlockHash;
    private String blockHash;
    private Certificate certificate;
    private long difficulty;
    private int winningNonce;
    private String ledgerHash;
    private List<String> transactions;
    private String minerSignature;
    private long minerSignatureIndex;

    public Block(long timestamp, int blockNum, String previousBlockHash, String blockHash, Certificate certificate, long difficulty, int winningNonce, String ledgerHash, List<String> transactions, String minerSignature, long minerSignatureIndex) {
        this.timestamp = timestamp;
        this.blockNum = blockNum;
        this.previousBlockHash = previousBlockHash;
        this.blockHash = blockHash;
        this.certificate = certificate;
        this.difficulty = difficulty;
        this.winningNonce = winningNonce;
        this.ledgerHash = ledgerHash;
        this.transactions = transactions;
        this.minerSignature = minerSignature;
        this.minerSignatureIndex = minerSignatureIndex;
        try {
            String transactionsString = "";
            //Transaction format: FromAddress;InputAmount;ToAddress1;Output1;ToAddress2;Output2... etc.
            for (int i = 0; i < transactions.size(); i++) {
                if (transactions.get(i).length() > 10) transactionsString += transactions.get(i) + "*";
            }
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            transactionsString = transactionsString.substring(0, transactionsString.length() - 1);
            String blockData = "{" + timestamp + ":" + blockNum + ":" + previousBlockHash + ":" + difficulty + ":" +
                    winningNonce + "},{" + ledgerHash + "},{" + transactionsString + "}," + certificate.getPublicKey().toString();
            this.blockHash = md.digest(blockData.getBytes("UTF-8")).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
