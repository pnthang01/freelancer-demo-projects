package com.etybeno.detectclient.model;

import com.etybeno.common.util.StringPool;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by thangpham on 23/05/2018.
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class IpLocationModel implements Serializable {

    public static final IpLocationModel UNKNOWN_LOCATION = new IpLocationModel();

    @JsonIgnore
    private int id = -1;
    @JsonIgnore
    private long ipFrom;
    @JsonIgnore
    private long ipTo;
    private String latitude = StringPool.MINUS;
    private String longitude = StringPool.MINUS;
    @JsonProperty("country_code")
    private String countryCode = StringPool.MINUS;
    @JsonProperty("country_name")
    private String countryName = StringPool.MINUS;
    @JsonProperty("region")
    private String region = StringPool.MINUS;
    @JsonProperty("city")
    private String city = StringPool.MINUS;
    @JsonProperty("isp_name")
    private String ispName = StringPool.MINUS;
    @JsonProperty("domain_name")
    private String domainName = StringPool.MINUS;
    private String mcc = StringPool.MINUS;
    private String mnc = StringPool.MINUS;
    @JsonProperty("mobile_brand")
    private String mobileBrand = StringPool.MINUS;
    @JsonProperty("usage_type")
    private String usageType = StringPool.MINUS;
    @JsonIgnore
    private int countryId = -1;
    @JsonIgnore
    private int regionId = -1;
    @JsonIgnore
    private int cityId = -1;
    @JsonIgnore
    private int ispId = -1;

    public int getId() {
        return id;
    }

    public IpLocationModel setId(int id) {
        this.id = id;
        return this;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getIspName() {
        return ispName;
    }

    public void setIspName(String ispName) {
        this.ispName = ispName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public String getMobileBrand() {
        return mobileBrand;
    }

    public void setMobileBrand(String mobileBrand) {
        this.mobileBrand = mobileBrand;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }

    public String getLatitude() {
        return latitude;
    }

    public IpLocationModel setLatitude(String latitude) {
        this.latitude = latitude;
        return this;
    }

    public String getLongitude() {
        return longitude;
    }

    public IpLocationModel setLongitude(String longitude) {
        this.longitude = longitude;
        return this;
    }

    public long getIpFrom() {
        return ipFrom;
    }

    public IpLocationModel setIpFrom(long ipFrom) {
        this.ipFrom = ipFrom;
        return this;
    }

    public long getIpTo() {
        return ipTo;
    }

    public IpLocationModel setIpTo(long ipTo) {
        this.ipTo = ipTo;
        return this;
    }

    public int getCountryId() {
        return countryId;
    }

    public IpLocationModel setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public int getRegionId() {
        return regionId;
    }

    public IpLocationModel setRegionId(int regionId) {
        this.regionId = regionId;
        return this;
    }

    public int getCityId() {
        return cityId;
    }

    public IpLocationModel setCityId(int cityId) {
        this.cityId = cityId;
        return this;
    }

    public int getIspId() {
        return ispId;
    }

    public IpLocationModel setIspId(int ispId) {
        this.ispId = ispId;
        return this;
    }

}
