package com.etybeno.detectclient.dao.redis;

import com.etybeno.common.util.MethodUtil;
import com.etybeno.common.util.StringUtil;
import com.etybeno.detectclient.model.*;
import com.etybeno.detectclient.service.LocationService;
import com.etybeno.detectclient.util.Constant;
import com.etybeno.redis.config.RedisInfo;
import com.etybeno.redis.config.RedisInfoConfiguration;
import com.etybeno.redis.service.RedisCommand;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by thangpham on 24/05/2018.
 */
public class IpLocationCacheRedis {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpLocationCacheRedis.class);

    private static IpLocationCacheRedis _instance;

    public synchronized static IpLocationCacheRedis _load() throws IOException, ConfigurationException {
        if (null == _instance) _instance = new IpLocationCacheRedis();
        return _instance;
    }

    private RedisInfo ipLocationCacheRedis;

    public IpLocationCacheRedis() throws ConfigurationException, IOException {
        ipLocationCacheRedis = RedisInfoConfiguration._load().getSingleRedisInfo(Constant.IP_LOCATION_CACHE_REDIS);
    }

    public CountryModel loadCountryModel(String countryCode2) {
        return new RedisCommand<CountryModel>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected CountryModel build() throws Exception {
                String idStr = jedis.hget("country_metadata", "country_code2:" + countryCode2);
                String hget = jedis.hget("country_metadata", "country_id:" + idStr);
                return StringUtil.OBJECT_MAPPER.readValue(hget, CountryModel.class);
            }
        }.execute();
    }

    public CountryModel loadCountryModel(int id) {
        return new RedisCommand<CountryModel>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected CountryModel build() throws Exception {
                String hget = jedis.hget("country_metadata", "country_id:" + id);
                return StringUtil.OBJECT_MAPPER.readValue(hget, CountryModel.class);
            }
        }.execute();
    }

    public boolean saveCountryModel(CountryModel countryModel) {
        if (null == countryModel && StringUtil.isNullOrEmpty(countryModel.getCountryCode2()))
            throw new IllegalArgumentException("Country model data is null or country code2 is null");
        return new RedisCommand<Boolean>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected Boolean build() throws Exception {
                Map<String, String> values = new HashMap<>();
                values.put("country_code2:" + countryModel.getCountryCode2(), countryModel.getId() + "");
                values.put("country_id:" + countryModel.getId(), StringUtil.OBJECT_MAPPER.writeValueAsString(countryModel));
                jedis.hmset("country_metadata", values);
                return true;
            }
        }.execute();
    }

    public RegionModel loadRegionModel(String regionName) {
        return new RedisCommand<RegionModel>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected RegionModel build() throws Exception {
                String idStr = jedis.hget("region_metadata", "region_name:" + regionName);
                String hget = jedis.hget("region_metadata", "region_id:" + idStr);
                return StringUtil.OBJECT_MAPPER.readValue(hget, RegionModel.class);
            }
        }.execute();
    }

    public RegionModel loadRegionModel(int id) {
        return new RedisCommand<RegionModel>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected RegionModel build() throws Exception {
                String hget = jedis.hget("region_metadata", "region_id:" + id);
                return StringUtil.OBJECT_MAPPER.readValue(hget, RegionModel.class);
            }
        }.execute();
    }

    public boolean saveRegionModel(RegionModel regionModel) {
        if (null == regionModel && StringUtil.isNullOrEmpty(regionModel.getRegionName()))
            throw new IllegalArgumentException("Region model data is null or region name is null");
        return new RedisCommand<Boolean>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected Boolean build() throws Exception {
                Map<String, String> values = new HashMap<>();
                values.put("region_name:" + regionModel.getRegionName(), regionModel.getId() + "");
                values.put("region_id:" + regionModel.getId(), StringUtil.OBJECT_MAPPER.writeValueAsString(regionModel));
                jedis.hset("region_metadata", "region_name:" + regionModel.getRegionName(),
                        StringUtil.OBJECT_MAPPER.writeValueAsString(regionModel));
                return true;
            }
        }.execute();
    }

    public CityModel loadCityModel(String cityName) {
        return new RedisCommand<CityModel>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected CityModel build() throws Exception {
                String idStr = jedis.hget("city_metadata", "city_name:" + cityName);
                String hget = jedis.hget("city_metadata", "city_id:" + idStr);
                return StringUtil.OBJECT_MAPPER.readValue(hget, CityModel.class);
            }
        }.execute();
    }

    public CityModel loadCityModel(int id) {
        return new RedisCommand<CityModel>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected CityModel build() throws Exception {
                String hget = jedis.hget("city_metadata", "city_id:" + id);
                return StringUtil.OBJECT_MAPPER.readValue(hget, CityModel.class);
            }
        }.execute();
    }

    public boolean saveCityModel(CityModel cityModel) {
        if (null == cityModel && StringUtil.isNullOrEmpty(cityModel.getCityName()))
            throw new IllegalArgumentException("City model data is null or city name is null");
        return new RedisCommand<Boolean>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected Boolean build() throws Exception {
                Map<String, String> values = new HashMap<>();
                values.put("city_name:" + cityModel.getCityName(), cityModel.getId() + "");
                values.put("city_id:" + cityModel.getId(), StringUtil.OBJECT_MAPPER.writeValueAsString(cityModel));
                jedis.hmset("city_metadata", values);
                return true;
            }
        }.execute();
    }

    public IspModel loadIspModel(int ispId) {
        return new RedisCommand<IspModel>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected IspModel build() throws Exception {
                String hget = jedis.hget("isp_metadata", "isp_id:" + ispId);
                return StringUtil.OBJECT_MAPPER.readValue(hget, IspModel.class);
            }
        }.execute();
    }

    public IspModel loadIspModel(String ispName, String usageType) {
        return new RedisCommand<IspModel>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected IspModel build() throws Exception {
                String idStr = jedis.hget("isp_metadata", "isp_name:" + ispName + "@^@" + usageType);
                String hget = jedis.hget("isp_metadata", "isp_id:" + idStr);
                return StringUtil.OBJECT_MAPPER.readValue(hget, IspModel.class);
            }
        }.execute();
    }

    public boolean saveIspModel(IspModel ispModel) {
        if (null == ispModel && StringUtil.isNullOrEmpty(ispModel.getIspName()))
            throw new IllegalArgumentException("Isp model data is null or isp name is null");
        return new RedisCommand<Boolean>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected Boolean build() throws Exception {
                Map<String, String> values = new HashMap<>();
                values.put("isp_name:" + ispModel.getIspName() + "@|@" + ispModel.getUsageType(), ispModel.getId() + "");
                values.put("isp_id:" + ispModel.getId(), StringUtil.OBJECT_MAPPER.writeValueAsString(ispModel));
                jedis.hmset("isp_metadata", values);
                return true;
            }
        }.execute();
    }

    public IpLocationModel getIpLocationModel(long ipAddress) {
        return new RedisCommand<IpLocationModel>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected IpLocationModel build() throws Exception {
                Set<String> floors = jedis.zrevrangeByScore("ip_location_range", String.valueOf(ipAddress), "0", 0, 1);
                Set<String> ceils = jedis.zrangeByScore("ip_location_range", String.valueOf(ipAddress), "4294967295", 0, 1);
                IpLocationModel location = null;
                if (floors.size() == 1 && ceils.size() == 1) {
                    String floor = floors.iterator().next();
                    String ceil = ceils.iterator().next();
                    if (floor.equals(ceil)) {
                        byte[] data = jedis.hget(("ip_location_data").getBytes(), floor.getBytes());
                        if (null != data) location = MethodUtil.deserializeBytes(data, IpLocationModel.class);
                    }
                }
                return location;
            }
        }.execute();
    }

    public boolean saveIpLocationModel(IpLocationModel locationModel) {
        if(null == locationModel || IpLocationModel.UNKNOWN_LOCATION == locationModel ||
                (locationModel.getIpTo() == 0 && locationModel.getIpFrom() == 0))
            throw new IllegalArgumentException("Could not save location model to redis");
        return new RedisCommand<Boolean>(ipLocationCacheRedis.getShardedJedisPool()) {
            @Override
            protected Boolean build() throws Exception {
                Pipeline pipelined = jedis.pipelined();
                String floor = locationModel.getIpFrom() + "-" + locationModel.getIpTo();
                pipelined.hset(("ip_location_data:" + floor).getBytes(), "data".getBytes(), MethodUtil.serializeObject(locationModel));
                pipelined.zadd("ip_location_range", locationModel.getIpFrom(), floor);
                pipelined.zadd("ip_location_range", locationModel.getIpTo(), floor);
                pipelined.sync();
                return true;
            }
        }.execute();
    }
}
