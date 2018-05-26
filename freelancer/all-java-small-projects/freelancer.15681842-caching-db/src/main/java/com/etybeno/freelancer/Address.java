package com.etybeno.freelancer;

public class Address {

    public String city;
    public String state;
    public String postalCode;
    public String countryCode;
    public String lines;

    public Address() {
    }

    public Address(String city, String state, String postalCode, String countryCode, String lines) {
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.countryCode = countryCode;
        this.lines = lines;

    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLines() {
        return lines;
    }

    public void setLines(String lines) {
        this.lines = lines;
    }
}
