package com.etybeno.detectclient.dao.mysql;

import com.etybeno.dbcp.base.SqlConnectionPool;
import com.etybeno.detectclient.model.*;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.etybeno.detectclient.util.Constant.IP_LOCATION_MYSQL;

/**
 * Created by thangpham on 22/05/2018.
 */
public class IpLocationDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpLocationDAO.class);


    public static void insertDataToIpLocation(List<Map<String, Object>> dataList) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        String query = "INSERT INTO ip_location (ip_from, ip_to, country_id, region_id, city_id" +
                ", isp_id, latitude, longitude) VALUES(?,?,?,?,?,?,?,?)";
        PreparedStatement statement = connection.prepareStatement(query);
        int effectCount = 0;
        try {
            for (Map<String, Object> model : dataList) {
                statement.setLong(1, (Long) model.get("ip_from"));
                statement.setLong(2, (Long) model.get("ip_to"));
                statement.setInt(3, (Integer) model.get("country_id"));
                statement.setInt(4, (Integer) model.get("region_id"));
                statement.setInt(5, (Integer) model.get("city_id"));
                statement.setInt(6, (Integer) model.get("isp_id"));
                statement.setString(7, (String) model.get("latitude"));
                statement.setString(8, (String) model.get("longitude"));
                statement.addBatch();
            }
            int[] ints = statement.executeBatch();
            effectCount = Arrays.stream(ints).parallel().sum();
        } catch (SQLException ex) {
            LOGGER.error("Error when insert data to country region", ex);
        } finally {
            closeConnection(statement, connection);
        }
    }

    public static int insertCountryDb(List<CountryModel> dataList) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        PreparedStatement statement = connection.prepareStatement("INSERT INTO country(country_name, country_code2, country_code3, country_numeric_code) VALUES(?,?,?,?)");
        int effectCount = 0;
        try {
            for (CountryModel model : dataList) {
                statement.setString(1, model.getCountryName());
                statement.setString(2, model.getCountryCode2());
                statement.setString(3, model.getCountryCode3());
                statement.setString(4, model.getCountryNumericCode());
                statement.addBatch();
            }
            int[] ints = statement.executeBatch();
            effectCount = Arrays.stream(ints).parallel().sum();
        } catch (SQLException ex) {
            LOGGER.error("Error when insert data to country region", ex);
        } finally {
            closeConnection(statement, connection);
        }
        return effectCount;
    }

    public static CountryModel getCountryFromDb(String countryCode2) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM country WHERE country_code2 = ?");
        CountryModel model = null;
        try {
            statement.setString(1, countryCode2);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                model = new CountryModel()
                        .setId(resultSet.getInt("id"))
                        .setCountryName(resultSet.getString("country_name"))
                        .setCountryCode2(resultSet.getString("country_code2"))
                        .setCountryCode2(resultSet.getString("country_code2"))
                        .setCountryNumericCode(resultSet.getString("country_numeric_code"));
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load country with country code 2 = %s", countryCode2), ex);
        } finally {
            closeConnection(statement, connection);
        }
        return null == model ? CountryModel.UNKNOWN_COUNTRY : model;
    }

    public static CountryModel getCountryFromDb(int id) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM country WHERE id = ?");
        CountryModel model = null;
        try {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                model = new CountryModel()
                        .setId(resultSet.getInt("id"))
                        .setCountryName(resultSet.getString("country_name"))
                        .setCountryCode2(resultSet.getString("country_code2"))
                        .setCountryCode2(resultSet.getString("country_code2"))
                        .setCountryNumericCode(resultSet.getString("country_numeric_code"));
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load country with country id = %d", id), ex);
        } finally {
            closeConnection(statement, connection);
        }
        return null == model ? CountryModel.UNKNOWN_COUNTRY : model;
    }

    public static int insertRegionDb(List<RegionModel> dataList) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        PreparedStatement statement = connection.prepareStatement("INSERT INTO region(region_name, region_code, country_id) VALUES(?,?,?)");
        int effectCount = 0;
        try {
            for (RegionModel model : dataList) {
                statement.setString(1, model.getRegionName());
                statement.setString(2, model.getRegionCode());
                statement.setInt(3, model.getCountryId());
                statement.addBatch();
            }
            int[] ints = statement.executeBatch();
            effectCount = Arrays.stream(ints).parallel().sum();
        } catch (SQLException ex) {
            LOGGER.error("Error when insert data to region region", ex);
        } finally {
            closeConnection(statement, connection);
        }
        return effectCount;
    }

    public static RegionModel getRegionFromDb(String regionName) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM region WHERE region_name = ?");
        RegionModel model = null;
        try {
            statement.setString(1, regionName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                model = new RegionModel()
                        .setId(resultSet.getInt("id"))
                        .setRegionName(resultSet.getString("region_name"))
                        .setRegionName(resultSet.getString("region_code"))
                        .setCountryId(resultSet.getInt("country_id"));
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load region with region name = %s", regionName), ex);
        } finally {
            closeConnection(statement, connection);
        }
        return null == model ? RegionModel.UNKNOWN_REGION : model;
    }

    public static RegionModel getRegionFromDb(int id) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM region WHERE id = ?");
        RegionModel model = null;
        try {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                model = new RegionModel()
                        .setId(resultSet.getInt("id"))
                        .setRegionName(resultSet.getString("region_name"))
                        .setRegionName(resultSet.getString("region_code"))
                        .setCountryId(resultSet.getInt("country_id"));
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load region with region id = %d", id), ex);
        } finally {
            closeConnection(statement, connection);
        }
        return null == model ? RegionModel.UNKNOWN_REGION : model;
    }

    public static int insertCityDb(List<CityModel> dataList) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        PreparedStatement statement = connection.prepareStatement("INSERT INTO city(city_name, region_id) VALUES(?,?)");
        int effectCount = 0;
        try {
            for (CityModel model : dataList) {
                statement.setString(1, model.getCityName());
                statement.setInt(2, model.getRegionId());
                statement.addBatch();
            }
            int[] ints = statement.executeBatch();
            effectCount = Arrays.stream(ints).parallel().sum();
        } catch (SQLException ex) {
            LOGGER.error("Error when insert data to city", ex);
            connection.rollback();
        } finally {
            closeConnection(statement, connection);
        }
        return effectCount;
    }

    public static CityModel getCityFromDb(String cityName) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM city WHERE city_name = ?");
        CityModel model = null;
        try {
            statement.setString(1, cityName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                model = new CityModel()
                        .setId(resultSet.getInt("id"))
                        .setCityName(resultSet.getString("city_name"))
                        .setRegionId(resultSet.getInt("region_id"));
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load city with city name = %s", cityName), ex);
        } finally {
            closeConnection(statement, connection);
        }
        return null == model ? CityModel.UNKNOWN_CITY : model;
    }

    public static CityModel getCityFromDb(int id) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM city WHERE id = ?");
        CityModel model = null;
        try {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                model = new CityModel()
                        .setId(resultSet.getInt("id"))
                        .setCityName(resultSet.getString("city_name"))
                        .setRegionId(resultSet.getInt("region_id"));
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load city with id = %d", id), ex);
        } finally {
            closeConnection(statement, connection);
        }
        return null == model ? CityModel.UNKNOWN_CITY : model;
    }

    public static int insertIspDb(List<IspModel> dataList) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        PreparedStatement statement = connection.prepareStatement("INSERT INTO isp" +
                "(isp_name, domain_name, mcc, mnc, mobile_brand, usage_type) VALUES(?,?,?,?,?,?)");
        int effectCount = 0;
        try {
            for (IspModel model : dataList) {
                statement.setString(1, model.getIspName());
                statement.setString(2, model.getDomainName());
                statement.setString(3, model.getMcc());
                statement.setString(4, model.getMnc());
                statement.setString(5, model.getMobileBrand());
                statement.setString(6, model.getUsageType());
                statement.addBatch();
            }
            int[] ints = statement.executeBatch();
            effectCount = Arrays.stream(ints).parallel().sum();
        } catch (SQLException ex) {
            LOGGER.error("Error when insert data to isp", ex);
        } finally {
            closeConnection(statement, connection);
        }
        return effectCount;
    }

    public static IspModel getIspFromDb(int id) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM isp WHERE id = ?");
        IspModel model = null;
        try {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                model = new IspModel()
                        .setId(resultSet.getInt("id"))
                        .setDomainName(resultSet.getString("domain_name"))
                        .setIspName(resultSet.getString("isp_name"))
                        .setMcc(resultSet.getString("mcc"))
                        .setMnc(resultSet.getString("mnc"))
                        .setMobileBrand(resultSet.getString("mobile_brand"))
                        .setUsageType(resultSet.getString("usage_type"));
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load isp with isp id = %d", id), ex);
        } finally {
            closeConnection(statement, connection);
        }
        return null == model ? IspModel.UNKNOWN_ISP : model;
    }

    public static IspModel getIspFromDb(String ispName, String usageType) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM isp WHERE isp_name = ? AND usage_type = ?");
        IspModel model = null;
        try {
            statement.setString(1, ispName);
            statement.setString(2, usageType);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                model = new IspModel()
                        .setId(resultSet.getInt("id"))
                        .setDomainName(resultSet.getString("domain_name"))
                        .setIspName(resultSet.getString("isp_name"))
                        .setMcc(resultSet.getString("mcc"))
                        .setMnc(resultSet.getString("mnc"))
                        .setMobileBrand(resultSet.getString("mobile_brand"))
                        .setUsageType(resultSet.getString("usage_type"));
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load isp with isp isp_name = %s & usage_type = %s", ispName, usageType), ex);
        } finally {
            closeConnection(statement, connection);
        }
        return null == model ? IspModel.UNKNOWN_ISP : model;
    }

    public static IpLocationModel getIpLocationModel(long ipAddress) throws ConfigurationException, SQLException {
        Connection connection = SqlConnectionPool._load().getConnection(IP_LOCATION_MYSQL);
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM ip_location WHERE ip_from <= ? AND ip_to >= ?");
        IpLocationModel model = null;
        try {
            statement.setLong(1, ipAddress);
            statement.setLong(2, ipAddress);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                model = new IpLocationModel()
                        .setId(resultSet.getInt("id"))
                        .setCityId(resultSet.getInt("city_id"))
                        .setCountryId(resultSet.getInt("country_id"))
                        .setRegionId(resultSet.getInt("region_id"))
                        .setIspId(resultSet.getInt("isp_id"))
                        .setIpFrom(resultSet.getLong("ip_from"))
                        .setIpTo(resultSet.getLong("ip_to"))
                        .setLatitude(resultSet.getString("latitude"))
                        .setLongitude(resultSet.getString("longitude"));
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Could not load location with ip_address = %d", ipAddress), ex);
        } finally {
            closeConnection(statement, connection);
        }
        return model;
    }


    private static void closeConnection(Statement statement, Connection connection) {
        if (null != statement) {
            try {
                statement.close(); //Silent close
            } catch (Exception ex) {
            }
        }
        if (null != connection) {
            try {
                connection.close();//Silent close
            } catch (Exception ex) {
            }
        }
    }

}
