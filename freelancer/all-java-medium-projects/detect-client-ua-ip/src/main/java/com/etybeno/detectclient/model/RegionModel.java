package com.etybeno.detectclient.model;

import com.etybeno.common.util.StringPool;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by thangpham on 24/05/2018.
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RegionModel {

    @JsonIgnore
    public static final RegionModel UNKNOWN_REGION = new RegionModel();

    private int id;
    @JsonProperty("rn")
    private String regionName = StringPool.MINUS;
    @JsonProperty("rc")
    private String regionCode = StringPool.MINUS;
    @JsonProperty("cid")
    private int countryId;

    public int getId() {
        return id;
    }

    public RegionModel setId(int id) {
        this.id = id;
        return this;
    }

    public String getRegionName() {
        return regionName;
    }

    public RegionModel setRegionName(String regionName) {
        this.regionName = regionName;
        return this;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public RegionModel setRegionCode(String regionCode) {
        this.regionCode = regionCode;
        return this;
    }

    public int getCountryId() {
        return countryId;
    }

    public RegionModel setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }
}
