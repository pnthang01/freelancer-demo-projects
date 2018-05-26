package com.etybeno.freelancer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommonAddressCache {

    private static CommonAddressCache _instance;

    public synchronized static CommonAddressCache loadCache() {
        if(null == _instance) _instance = new CommonAddressCache();
        return _instance;
    }

    private ConcurrentMap<String, Address> cachingMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Address> dbAddressMap = new ConcurrentHashMap<>();

    private Address createAddress(String lines, String city, String state, String postalCode,
                                 String countryCode) {
        Address address = new Address();
        // Set the information
        address.setLines(lines);
        address.setCity(city);
        address.setState(state);
        address.setPostalCode(postalCode);
        address.setCountryCode(countryCode);
        return address;
    }

    /**
     * This function will search in DB. If the address does not exist, it will insert and return null.
     * Otherwise it return an address object.
     * @param lines
     * @param city
     * @param state
     * @param postalCode
     * @param countryCode
     * @return
     */
    public Address SearchDB(String lines, String city, String state, String postalCode, String countryCode) {
        Address address =  createAddress(lines, city, state, postalCode, countryCode);
        //Simulate db query, although it sames as cache key, but mechanism can be changed
        String query = buildQueryFromAddress(address);
        try {
            Thread.sleep(5000); //Simulate slow query DB.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Address dbAddress = dbAddressMap.putIfAbsent(query, address);
        if(null == dbAddress) {
            System.out.println(query + " does not exist in DB");
            return address;
        } else return dbAddress;
    }

    /**
     * Caching mechanism
     * @param lines
     * @param city
     * @param state
     * @param postalCode
     * @param countryCode
     * @return
     */
    public Address getAddress(String lines, String city, String state, String postalCode, String countryCode) {
        String cacheKey = lines + "$" + city + "$" + state + "$" + postalCode + "$" + countryCode;
        System.out.println("Start to get cache=" + cacheKey);
        long s = System.currentTimeMillis() / 1000;
        Address rs;
        if(cachingMap.containsKey(cacheKey)) { // Check whether cache contains this address
            System.out.println(cacheKey + " already in cache");
            rs = cachingMap.get(cacheKey);
        } else {
            //This line already search or insert new record to db
            Address address = SearchDB(lines, city, state, postalCode, countryCode);
            cachingMap.put(cacheKey, address);
            rs = address;
        }
        System.out.println(String.format("Get cache [%s] takes %d seconds", cacheKey, (System.currentTimeMillis() / 1000 - s)));
        return rs;
    }

    private String buildQueryFromAddress(Address address) {
        if(null == address) return null;
        return address.getLines() + "$" + address.getCity() + "$" + address.getState() + "$" +
                address.getPostalCode() + "$" + address.getCountryCode();
    }
}