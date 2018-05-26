package com.etybeno.detectclient.cache;

import com.etybeno.common.model.FileStateModel;
import com.etybeno.common.util.StringUtil;
import com.etybeno.detectclient.dao.mysql.IpLocationDAO;
import com.etybeno.detectclient.dao.redis.IpLocationCacheRedis;
import com.etybeno.detectclient.model.CityModel;
import com.etybeno.detectclient.model.CountryModel;
import com.etybeno.detectclient.model.IspModel;
import com.etybeno.detectclient.model.RegionModel;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Created by thangpham on 24/05/2018.
 */
public class IpLocationInfoCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpLocationInfoCache.class);

    private static IpLocationInfoCache _instance;

    public synchronized static IpLocationInfoCache _load() throws IOException, ConfigurationException {
        if (null == _instance) _instance = new IpLocationInfoCache();
        return _instance;
    }

    private IpLocationCacheRedis ipLocationRedis;

    public IpLocationInfoCache() throws IOException, ConfigurationException {
        ipLocationRedis = IpLocationCacheRedis._load();
    }

    public LoadingCache<String, FileStateModel> fileStateList = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, FileStateModel>() {
                        public FileStateModel load(String key) throws Exception {
                            return new FileStateModel(key);
                        }
                    });

    public LoadingCache<Integer, CountryModel> countryList() {
        return countryList;
    }

    public LoadingCache<String, CountryModel> countries() {
        return countries;
    }

    private LoadingCache<Integer, CountryModel> countryList = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<Integer, CountryModel>() {
                        public CountryModel load(Integer key) throws Exception {
                            return loadCountry(key);
                        }
                    });

    private LoadingCache<String, CountryModel> countries = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, CountryModel>() {
                        public CountryModel load(String key) throws Exception {
                            return loadCountry(key);
                        }
                    });

    private CountryModel loadCountry(int id) {
        CountryModel countryModel = null;
        try {
            countryModel = ipLocationRedis.loadCountryModel(id);
            if (null == countryModel) {
                countryModel = IpLocationDAO.getCountryFromDb(id);
                if (CountryModel.UNKNOWN_COUNTRY != countryModel) ipLocationRedis.saveCountryModel(countryModel);
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load country with country-id: %d", id), ex);
        }
        return countryModel;
    }

    private CountryModel loadCountry(String countryCode2) {
        CountryModel countryModel = null;
        try {
            countryModel = ipLocationRedis.loadCountryModel(countryCode2);
            if (null == countryModel) {
                countryModel = IpLocationDAO.getCountryFromDb(countryCode2);
                if (CountryModel.UNKNOWN_COUNTRY != countryModel) ipLocationRedis.saveCountryModel(countryModel);
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load country with country-code2: %s", countryCode2), ex);
        }
        System.out.println("Load 2 " + StringUtil.GSON.toJson(countryModel));
        return countryModel;
    }

    //===============================

    public LoadingCache<Integer, RegionModel> regionList() {
        return regionList;
    }

    public LoadingCache<String, RegionModel> regions() {
        return regions;
    }

    private LoadingCache<Integer, RegionModel> regionList = CacheBuilder.newBuilder()
            .maximumSize(4000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<Integer, RegionModel>() {
                        @Override
                        public RegionModel load(Integer key) throws Exception {
                            return loadRegion(key);
                        }
                    });

    private LoadingCache<String, RegionModel> regions = CacheBuilder.newBuilder()
            .maximumSize(4000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, RegionModel>() {
                        @Override
                        public RegionModel load(String key) throws Exception {
                            return loadRegion(key);
                        }
                    });


    private RegionModel loadRegion(String regionName) {
        RegionModel regionModel = null;
        try {
            regionModel = ipLocationRedis.loadRegionModel(regionName);
            if (null == regionModel) {
                regionModel = IpLocationDAO.getRegionFromDb(regionName);
                if (RegionModel.UNKNOWN_REGION != regionModel) ipLocationRedis.saveRegionModel(regionModel);
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load region with region-name: %s", regionName), ex);
        }
        return regionModel;
    }

    private RegionModel loadRegion(int id) {
        RegionModel regionModel = null;
        try {
            regionModel = ipLocationRedis.loadRegionModel(id);
            if (null == regionModel) {
                regionModel = IpLocationDAO.getRegionFromDb(id);
                if (RegionModel.UNKNOWN_REGION != regionModel) ipLocationRedis.saveRegionModel(regionModel);
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load region with region-id: %d", id), ex);
        }
        return regionModel;
    }

    //======================================

    public LoadingCache<Integer, CityModel> cityList() { return cityList;}

    private LoadingCache<Integer, CityModel> cityList = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<Integer, CityModel>() {
                        @Override
                        public CityModel load(Integer key) throws Exception {
                            return loadCity(key);
                        }
                    });

    public LoadingCache<String, CityModel> cities() {
        return cities;
    }

    private LoadingCache<String, CityModel> cities = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, CityModel>() {
                        @Override
                        public CityModel load(String key) throws Exception {
                            return loadCity(key);
                        }
                    });


    private CityModel loadCity(int id) {
        CityModel cityModel = null;
        try {
            cityModel = ipLocationRedis.loadCityModel(id);
            if (null == cityModel) {
                cityModel = IpLocationDAO.getCityFromDb(id);
                if (CityModel.UNKNOWN_CITY != cityModel) ipLocationRedis.saveCityModel(cityModel);
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load city with city-id: %d", id), ex);
        }
        return cityModel;
    }

    private CityModel loadCity(String cityName) {
        CityModel cityModel = null;
        try {
            cityModel = ipLocationRedis.loadCityModel(cityName);
            if (null == cityModel) {
                cityModel = IpLocationDAO.getCityFromDb(cityName);
                if (CityModel.UNKNOWN_CITY != cityModel) ipLocationRedis.saveCityModel(cityModel);
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load city with city-name: %s", cityName), ex);
        }
        return cityModel;
    }

    //================================

    public LoadingCache<Integer, IspModel> ispList() {
        return ispList;
    }

    public LoadingCache<String, IspModel> isps() {return isps;}

    private LoadingCache<String, IspModel> isps = CacheBuilder.newBuilder()
            .maximumSize(200000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, IspModel>() {
                        @Override
                        public IspModel load(String key) throws Exception {
                            return loadIsp(key);
                        }
                    });

    private LoadingCache<Integer, IspModel> ispList = CacheBuilder.newBuilder()
            .maximumSize(200000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<Integer, IspModel>() {
                        @Override
                        public IspModel load(Integer key) throws Exception {
                            return loadIsp(key);
                        }
                    });

    private IspModel loadIsp(int id) {
        IspModel ispModel = null;
        try {
            ispModel = ipLocationRedis.loadIspModel(id);
            if (null == ispModel) {
                ispModel = IpLocationDAO.getIspFromDb(id);
                if (IspModel.UNKNOWN_ISP != ispModel) ipLocationRedis.saveIspModel(ispModel);
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load isp with isp-id: %d", ispModel), ex);
        }
        return ispModel;
    }

    private IspModel loadIsp(String key) {
        IspModel ispModel = null;
        String[] split = key.split("@_@");
        try {
            ispModel = ipLocationRedis.loadIspModel(split[0], split[1]);
            if (null == ispModel) {
                ispModel = IpLocationDAO.getIspFromDb(split[0], split[1]);
                if (IspModel.UNKNOWN_ISP != ispModel) ipLocationRedis.saveIspModel(ispModel);
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load isp with isp-key: %s", key), ex);
        }
        return ispModel;
    }
}