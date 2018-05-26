package com.etybeno.detectclient.model;

import com.etybeno.common.util.StringPool;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by thangpham on 24/05/2018.
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class CountryModel {

    @JsonIgnore
    public static CountryModel UNKNOWN_COUNTRY = new CountryModel();

    private int id;
    @JsonProperty("cn")
    private String countryName = StringPool.MINUS;
    @JsonProperty("cc2")
    private String countryCode2 = StringPool.MINUS;
    @JsonProperty("cc3")
    private String countryCode3 = StringPool.MINUS;
    @JsonProperty("cnc")
    private String countryNumericCode = StringPool.MINUS;

    public int getId() {
        return id;
    }

    public CountryModel setId(int id) {
        this.id = id;
        return this;
    }

    public String getCountryName() {
        return countryName;
    }

    public CountryModel setCountryName(String countryName) {
        this.countryName = countryName;
        return this;
    }

    public String getCountryCode2() {
        return countryCode2;
    }

    public CountryModel setCountryCode2(String countryCode2) {
        this.countryCode2 = countryCode2;
        return this;
    }

    public String getCountryCode3() {
        return countryCode3;
    }

    public CountryModel setCountryCode3(String countryCode3) {
        this.countryCode3 = countryCode3;
        return this;
    }

    public String getCountryNumericCode() {
        return countryNumericCode;
    }

    public CountryModel setCountryNumericCode(String countryNumericCode) {
        this.countryNumericCode = countryNumericCode;
        return this;
    }
}
