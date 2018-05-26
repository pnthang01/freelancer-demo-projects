package com.etybeno.detectclient.model;

import com.etybeno.common.util.StringPool;

/**
 * Created by thangpham on 23/05/2018.
 */
public class CountryRegionModel {

    private int id;
    private String countryName = StringPool.MINUS;
    private String countryCode2 = StringPool.MINUS;
    private String countryCode3 = StringPool.MINUS;
    private String countryNumericCode = StringPool.MINUS;
    private String regionName = StringPool.MINUS;
    private String regionCode = StringPool.MINUS;
    private String cityName = StringPool.MINUS;

    public String getCityName() {
        return cityName;
    }

    public CountryRegionModel setCityName(String cityName) {
        this.cityName = cityName;
        return this;
    }

    public int getId() {
        return id;
    }

    public CountryRegionModel setId(int id) {
        this.id = id;
        return this;
    }

    public String getCountryName() {
        return countryName;
    }

    public CountryRegionModel setCountryName(String countryName) {
        this.countryName = countryName;
        return this;
    }

    public String getCountryCode2() {
        return countryCode2;
    }

    public CountryRegionModel setCountryCode2(String countryCode2) {
        this.countryCode2 = countryCode2;
        return this;
    }

    public String getCountryCode3() {
        return countryCode3;
    }

    public CountryRegionModel setCountryCode3(String countryCode3) {
        this.countryCode3 = countryCode3;
        return this;
    }

    public String getCountryNumericCode() {
        return countryNumericCode;
    }

    public CountryRegionModel setCountryNumericCode(String countryNumericCode) {
        this.countryNumericCode = countryNumericCode;
        return this;
    }

    public String getRegionName() {
        return regionName;
    }

    public CountryRegionModel setRegionName(String regionName) {
        this.regionName = regionName;
        return this;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public CountryRegionModel setRegionCode(String regionCode) {
        this.regionCode = regionCode;
        return this;
    }
}
