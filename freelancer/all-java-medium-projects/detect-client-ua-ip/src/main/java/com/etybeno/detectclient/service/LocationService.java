package com.etybeno.detectclient.service;

import com.etybeno.common.util.StringUtil;
import com.etybeno.detectclient.cache.IpLocationInfoCache;
import com.etybeno.detectclient.dao.mysql.IpLocationDAO;
import com.etybeno.detectclient.dao.redis.IpLocationCacheRedis;
import com.etybeno.detectclient.model.*;
import com.google.common.cache.*;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by thangpham on 25/05/2018.
 */
public class LocationService {

    private static Logger LOGGER = LoggerFactory.getLogger(LocationService.class);

    private static LocationService _instance;

    public synchronized static LocationService _load() throws IOException, ConfigurationException {
        if (null == _instance) _instance = new LocationService();
        return _instance;
    }

    private ConcurrentNavigableMap<Long, IpLocationModel> LOCATION_CACHE = new ConcurrentSkipListMap<>();

    private LoadingCache<Long, Boolean> CACHE_KEEPER = CacheBuilder.newBuilder()
            .maximumSize(1000000)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .removalListener(new CacheKeeperRemovalListener())
            .build(
                    new CacheLoader<Long, Boolean>() {
                        public Boolean load(Long key) throws Exception {
                            return Boolean.TRUE;
                        }
                    });

    private IpLocationInfoCache infoCache;
    private IpLocationCacheRedis locationRedis;

    private LocationService() throws IOException, ConfigurationException {
        infoCache = IpLocationInfoCache._load();
        locationRedis = IpLocationCacheRedis._load();
    }

    /**
     * Lookup province ip, có cache các dãy IP của HCM, HA NOI (request lớn)
     * ---> improve performance đáng kể
     *
     * @param ipAdress
     * @return provinceId
     */
    public IpLocationModel getLocationFromIpAddress(String ipAdress) {
        IpLocationModel locationCacheObj = null;
        long ipLong = 0;
        try {
            ipLong = StringUtil.Dot2LongIP(ipAdress);
            IpLocationModel floorCacheObj = LOCATION_CACHE.get(LOCATION_CACHE.floorKey(ipLong)); // chận dưới lớn nhất
            IpLocationModel ceilCacheObj = LOCATION_CACHE.get(LOCATION_CACHE.ceilingKey(ipLong)); // chận trên nhỏ nhất
            if (null != floorCacheObj && floorCacheObj == ceilCacheObj) locationCacheObj = floorCacheObj;
            else {
                locationCacheObj = locationRedis.getIpLocationModel(ipLong);
                if (null == locationCacheObj) {
                    locationCacheObj = IpLocationDAO.getIpLocationModel(ipLong);
                    if (null == locationCacheObj) {
                        loadLocationData(locationCacheObj);
                        locationRedis.saveIpLocationModel(locationCacheObj);
                    }
                }
                if (null == locationCacheObj) locationCacheObj = IpLocationModel.UNKNOWN_LOCATION;
                else {
                    LOCATION_CACHE.put(locationCacheObj.getIpFrom(), locationCacheObj);
                    LOCATION_CACHE.put(locationCacheObj.getIpTo(), locationCacheObj);
                }
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load data for ip %s", ipAdress), ex);
        }
        return locationCacheObj;
    }

    private void loadLocationData(IpLocationModel model) throws ExecutionException {
        if (model.getCountryId() > 0) {
            CountryModel countryModel = infoCache.countryList().get(model.getCountryId());
            model.setCountryCode(countryModel.getCountryCode2());
            model.setCountryName(countryModel.getCountryName());
        }
        if (model.getRegionId() > 0) {
            RegionModel regionModel = infoCache.regionList().get(model.getRegionId());
            model.setRegion(regionModel.getRegionName());
        }
        if (model.getCityId() > 0) {
            CityModel cityModel = infoCache.cityList().get(model.getCityId());
            model.setCity(cityModel.getCityName());
        }
        if (model.getIspId() > 0) {
            IspModel ispModel = infoCache.ispList().get(model.getIspId());
            model.setIspName(ispModel.getIspName());
            model.setDomainName(ispModel.getDomainName());
            model.setMcc(ispModel.getMcc());
            model.setMnc(ispModel.getMcc());
            model.setMobileBrand(ispModel.getMobileBrand());
            model.setUsageType(ispModel.getUsageType());
        }
    }

    private class CacheKeeperRemovalListener implements RemovalListener<Long, Boolean> {

        @Override
        public void onRemoval(RemovalNotification<Long, Boolean> notification) {
            Long key = notification.getKey();
            LOCATION_CACHE.remove(key);
        }
    }

}
