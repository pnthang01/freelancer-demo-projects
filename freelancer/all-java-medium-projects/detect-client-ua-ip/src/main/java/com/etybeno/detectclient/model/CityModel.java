package com.etybeno.detectclient.model;

import com.etybeno.common.util.StringPool;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by thangpham on 24/05/2018.
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class CityModel {

    public static final CityModel UNKNOWN_CITY = new CityModel();

    private int id;
    @JsonProperty("cn")
    private String cityName = StringPool.MINUS;
    @JsonProperty("rid")
    private int regionId;

    public int getId() {
        return id;
    }

    public CityModel setId(int id) {
        this.id = id;
        return this;
    }

    public String getCityName() {
        return cityName;
    }

    public CityModel setCityName(String cityName) {
        this.cityName = cityName;
        return this;
    }

    public int getRegionId() {
        return regionId;
    }

    public CityModel setRegionId(int regionId) {
        this.regionId = regionId;
        return this;
    }
}
