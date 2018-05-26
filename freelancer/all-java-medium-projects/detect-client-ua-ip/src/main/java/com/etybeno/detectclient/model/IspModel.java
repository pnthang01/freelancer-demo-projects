package com.etybeno.detectclient.model;

import com.etybeno.common.util.StringPool;

/**
 * Created by thangpham on 25/05/2018.
 */
public class IspModel {

    public static final IspModel UNKNOWN_ISP = new IspModel();

    private int id;
    private String ispName = StringPool.MINUS;
    private String domainName = StringPool.MINUS;
    private String mcc = StringPool.MINUS;
    private String mnc = StringPool.MINUS;
    private String mobileBrand = StringPool.MINUS;
    private String usageType = StringPool.MINUS;

    public int getId() {
        return id;
    }

    public IspModel setId(int id) {
        this.id = id;
        return this;
    }

    public String getIspName() {
        return ispName;
    }

    public IspModel setIspName(String ispName) {
        this.ispName = ispName;
        return this;
    }

    public String getDomainName() {
        return domainName;

    }

    public IspModel setDomainName(String domainName) {
        this.domainName = domainName;
        return this;
    }

    public String getMcc() {
        return mcc;
    }

    public IspModel setMcc(String mcc) {
        this.mcc = mcc;
        return this;
    }

    public String getMnc() {
        return mnc;
    }

    public IspModel setMnc(String mnc) {
        this.mnc = mnc;
        return this;
    }

    public String getMobileBrand() {
        return mobileBrand;
    }

    public IspModel setMobileBrand(String mobileBrand) {
        this.mobileBrand = mobileBrand;
        return this;
    }

    public String getUsageType() {
        return usageType;
    }

    public IspModel setUsageType(String usageType) {
        this.usageType = usageType;
        return this;
    }
}
