package com.etybeno.detectclient;

import com.etybeno.common.model.FileStateModel;
import com.etybeno.common.util.FileUtil;
import com.etybeno.common.util.StringUtil;
import com.etybeno.detectclient.cache.IpLocationInfoCache;
import com.etybeno.detectclient.dao.mysql.IpLocationDAO;
import com.etybeno.detectclient.model.*;
import com.google.common.collect.Lists;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by thangpham on 22/05/2018.
 */
public class IpLocationLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpLocationLoader.class);

    private final int DEFAULT_SIZE = 10000;

    public static void main(String[] args) throws Exception {
        IpLocationLoader loader = new IpLocationLoader();
//        loader.loadCountryToDb();
//        loader.loadRegionToDb();
        loader.loadIpLocationToDb();
    }

    public void loadIpLocationToDb() throws SQLException, ConfigurationException, InterruptedException, IOException, ExecutionException {
        List<String> ipData = null;
        AtomicInteger i = new AtomicInteger();
        IpLocationInfoCache infoCache = IpLocationInfoCache._load();
        do {
            ipData = loadDataFromFile("/home/thangpham/Downloads/iplocation/iplocation.CSV", false);
            ExecutorService executorService = Executors.newFixedThreadPool(6);
            List<List<String>> partition = Lists.partition(ipData, 100);
            System.out.println(ipData.size());
            for (List<String> splitList : partition) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        int thread = i.incrementAndGet();
                        long s = System.currentTimeMillis();
                        List<String> data = new ArrayList<>(splitList);
                        List<Map<String, Object>> collect = data.stream()
                                .map(str -> {
                                    String[] split = str.split("\",\"");
                                    Map<String, Object> map = new HashMap();
                                    try {
                                        CountryModel countryModel = infoCache.countries().get(split[2].replaceAll("\"", ""));
                                        RegionModel regionModel = infoCache.regions().get(split[4].replaceAll("\"", ""));
                                        String cityName = split[5].replaceAll("\"", "");
                                        CityModel cityModel = infoCache.cities().get(cityName);
                                        if (CityModel.UNKNOWN_CITY == cityModel) {
                                            cityModel = new CityModel();
                                            cityModel.setCityName(cityName);
                                            IpLocationDAO.insertCityDb(Arrays.asList(cityModel));
                                            infoCache.cities().invalidate(cityName);
                                            cityModel = infoCache.cities().get(cityName);
                                        }
                                        String ispName = split[8].replaceAll("\"", "");
                                        String usageType = split[13].replaceAll("\"", "");
                                        String ispKey = ispName + "@_@" + usageType;
                                        IspModel ispModel = infoCache.isps().get(ispKey);
                                        if (IspModel.UNKNOWN_ISP == ispModel) {
                                            ispModel = new IspModel()
                                                    .setIspName(ispName)
                                                    .setDomainName(split[9].replaceAll("\"", ""))
                                                    .setMcc(split[10].replaceAll("\"", ""))
                                                    .setMnc(split[11].replaceAll("\"", ""))
                                                    .setMobileBrand(split[12].replaceAll("\"", ""))
                                                    .setUsageType(usageType);
                                            IpLocationDAO.insertIspDb(Arrays.asList(ispModel));
                                            infoCache.isps().invalidate(ispKey);
                                            ispModel = infoCache.isps().get(ispKey);
                                        }
                                        map.put("ip_from", Long.valueOf(split[0].replaceAll("\"", "")));
                                        map.put("ip_to", Long.valueOf(split[1].replaceAll("\"", "")));
                                        map.put("country_id", countryModel.getId());
                                        map.put("region_id", regionModel.getId());
                                        map.put("city_id", cityModel.getId());
                                        map.put("isp_id", ispModel.getId());
                                        map.put("latitude", split[6].replaceAll("\"", ""));
                                        map.put("longitude", split[7].replaceAll("\"", ""));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    return map;
                                })
                                .collect(Collectors.toList());
                        try {
                            IpLocationDAO.insertDataToIpLocation(collect);
                        } catch (ConfigurationException e) {
                            e.printStackTrace();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        LOGGER.info(String.format("Thead %d takes %d miliseconds", thread, (System.currentTimeMillis() - s)));
                    }
                });
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executorService.shutdown();
            while (!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
                System.out.println("Wait the batch to inserted");
            }
            System.out.println("Insert for " + i);
        } while (ipData.size() >= DEFAULT_SIZE);
        System.out.println(i);
    }

    public void loadCountryToDb() throws ExecutionException, ConfigurationException, IOException {
        List<String> countryData = loadDataFromFile("/home/thangpham/Downloads/iplocation/IP2LOCATION-COUNTRY.CSV", true);
        countryData.remove(0);
        List<CountryModel> countries = countryData.parallelStream()
                .map(str -> {
                    String[] split = str.split("\",\"");
                    return new CountryModel()
                            .setCountryName(split[0].replaceAll("\"", ""))
                            .setCountryCode2(split[1].replaceAll("\"", ""))
                            .setCountryCode3(split[2].replaceAll("\"", ""))
                            .setCountryNumericCode(split[3].replaceAll("\"", ""));
                })
                .collect(Collectors.toList());
        //
        LOGGER.info(String.format("Loaded %d country model data from csv", countries.size()));
        try {
            int i = IpLocationDAO.insertCountryDb(countries);
            LOGGER.info("Insert " + i + " countries");
        } catch (Exception ex) {
            LOGGER.error("Error when insert data to country table", ex);
        }
    }

    public void loadRegionToDb() throws IOException, ConfigurationException, ExecutionException {
        List<String> regionData = loadDataFromFile("/home/thangpham/Downloads/iplocation/IP2LOCATION-COUNTRY-REGION.CSV", true);
        regionData.remove(0);
        IpLocationInfoCache locationCache = IpLocationInfoCache._load();
        List<RegionModel> regions = regionData.parallelStream()
                .map(str -> {
                    String[] split = str.split("\",\"");
                    int countryId = 0;
                    try {
                        CountryModel countryModel = locationCache.countries().get(split[0].replaceAll("\"", ""));
                        countryId = countryModel.getId();
                    } catch (ExecutionException e) {
                        LOGGER.error(String.format("Could not load country code:%s", split[0]), e);
                    }
                    return new RegionModel()
                            .setRegionCode(split[3].replaceAll("\"", ""))
                            .setRegionName(split[4].replaceAll("\"", ""))
                            .setCountryId(countryId);
                })
                .collect(Collectors.toList());
        //
        LOGGER.info(String.format("Loaded %d region model data from csv", regions.size()));
        try {
            int i = IpLocationDAO.insertRegionDb(regions);
            LOGGER.info("Insert " + i + " regions");
        } catch (Exception ex) {
            LOGGER.error("Error when insert data to region table", ex);
        }
    }

    private List<String> loadDataFromFile(String fileLoc, boolean all) throws IOException, ConfigurationException, ExecutionException {
        File file = new File(fileLoc);
        if (!file.exists()) throw new IllegalArgumentException("File must be exist");
        FileStateModel model = IpLocationInfoCache._load().fileStateList.get(file.getName());
        List<String> unreadRecords = new ArrayList<>();
        List<String> temp;
        do {
            temp = FileUtil.getUnreadRecords(file, model, DEFAULT_SIZE, 0, 0);
            unreadRecords.addAll(temp);
        } while (all && temp.size() >= DEFAULT_SIZE);
        return unreadRecords;
    }

}
