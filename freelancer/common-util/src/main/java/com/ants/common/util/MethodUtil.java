package com.ants.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Created by thangpham on 08/09/2017.
 */
public class MethodUtil {

    public static final int BITSET_SIZE = 400000000;
    static final Logger LOGGER = LogManager.getLogger(MethodUtil.class.getName());
    private static final String TAG = MethodUtil.class.toString();
    private static final Gson gson = new Gson();
    private static final Gson excludeJson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
            .create();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Type MAP_JSON_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    private static final Type STRING_LIST_JSON_TYPE = new TypeToken<List<String>>() {
    }.getType();

    private static final Type INTEGER_LIST_JSON_TYPE = new TypeToken<List<Integer>>() {
    }.getType();

    private static final Type LIST_JSON_TYPE = new TypeToken<List>() {
    }.getType();

    // data for normalize vietnamese text
    private static final char[] SPECIAL_CHARACTERS = {'!', '"', '#', '$', '%',
            '*', '+', ',', ':', '<', '=', '>', '?', '@', '[', '\\', ']', '^',
            '`', '|', '~', 'À', 'Á', 'Â', 'Ã', 'È', 'É', 'Ê', 'Ì', 'Í', 'Ò',
            'Ó', 'Ô', 'Õ', 'Ù', 'Ú', 'Ý', 'à', 'á', 'â', 'ã', 'è', 'é', 'ê',
            'ì', 'í', 'ò', 'ó', 'ô', 'õ', 'ù', 'ú', 'ý', 'Ă', 'ă', 'Đ', 'đ',
            'Ĩ', 'ĩ', 'Ũ', 'ũ', 'Ơ', 'ơ', 'Ư', 'ư', 'Ạ', 'ạ', 'Ả', 'ả', 'Ấ',
            'ấ', 'Ầ', 'ầ', 'Ẩ', 'ẩ', 'Ẫ', 'ẫ', 'Ậ', 'ậ', 'Ắ', 'ắ', 'Ằ', 'ằ',
            'Ẳ', 'ẳ', 'Ẵ', 'ẵ', 'Ặ', 'ặ', 'Ẹ', 'ẹ', 'Ẻ', 'ẻ', 'Ẽ', 'ẽ', 'Ế',
            'ế', 'Ề', 'ề', 'Ể', 'ể', 'Ễ', 'ễ', 'Ệ', 'ệ', 'Ỉ', 'ỉ', 'Ị', 'ị',
            'Ọ', 'ọ', 'Ỏ', 'ỏ', 'Ố', 'ố', 'Ồ', 'ồ', 'Ổ', 'ổ', 'Ỗ', 'ỗ', 'Ộ',
            'ộ', 'Ớ', 'ớ', 'Ờ', 'ờ', 'Ở', 'ở', 'Ỡ', 'ỡ', 'Ợ', 'ợ', 'Ụ', 'ụ',
            'Ủ', 'ủ', 'Ứ', 'ứ', 'Ừ', 'ừ', 'Ử', 'ử', 'Ữ', 'ữ', 'Ự', 'ự',};

    private static final char[] REPLACEMENTS = {'\0', '\0', '\0', '\0', '\0',
            '\0', '_', '\0', '_', '\0', '\0', '\0', '\0', '\0', '\0', '_',
            '\0', '\0', '\0', '\0', '\0', 'A', 'A', 'A', 'A', 'E', 'E', 'E',
            'I', 'I', 'O', 'O', 'O', 'O', 'U', 'U', 'Y', 'a', 'a', 'a', 'a',
            'e', 'e', 'e', 'i', 'i', 'o', 'o', 'o', 'o', 'u', 'u', 'y', 'A',
            'a', 'D', 'd', 'I', 'i', 'U', 'u', 'O', 'o', 'U', 'u', 'A', 'a',
            'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A',
            'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'E', 'e', 'E', 'e',
            'E', 'e', 'E', 'e', 'E', 'e', 'E', 'e', 'E', 'e', 'E', 'e', 'I',
            'i', 'I', 'i', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o',
            'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O',
            'o', 'U', 'u', 'U', 'u', 'U', 'u', 'U', 'u', 'U', 'u', 'U', 'u',
            'U', 'u',};

    public static String toJsonJackson(Object o) throws IOException {
        return mapper.writeValueAsString(o);
    }

    public static Map<String, String> fromJsonToMap(String json) {
        return gson.fromJson(json, MAP_JSON_TYPE);
    }

    public static List<String> fromJsonToStringList(String json) {
        return gson.fromJson(json, STRING_LIST_JSON_TYPE);
    }

    public static List<Integer> fromJsonToIntegerList(String json) {
        return gson.fromJson(json, INTEGER_LIST_JSON_TYPE);
    }

    public static List fromJsonToList(String json) {
        return gson.fromJson(json, LIST_JSON_TYPE);
    }

    public static String convertAgeRange(String ageRange) {
        if (null == ageRange || ageRange.isEmpty()) {
            return "-";
        }
        ageRange = ageRange.trim();
        String result = null;
        switch (ageRange) {
            case "0-24":
            case "18-24":
            case "13-17":
            case "18-30":
                result = "18-24";
                break;
            case "25-34":
                result = "25-34";
                break;
            case "31-45":
            case "35-54":
                result = "35-44";
                break;
            case "46-60":
                result = "45-54";
                break;
            case "55+":
                result = "55+";
                break;
            default:
                result = "-";
                break;
        }
        return result;
    }

    public static Map<String, String> parseUtmFromUrlNetty(String url) {
        Map<String, String> result = new HashMap();
        try {
            if (!StringUtil.isNullOrEmpty(url)) {
                QueryStringDecoder queryStringDecoder = new QueryStringDecoder(url);
                Map<String, List<String>> params = queryStringDecoder.parameters();
                result.put("utmMedium", getParamValue("utm_medium", params));
                result.put("utmSource", getParamValue("utm_source", params));
                result.put("utmCampaign", getParamValue("utm_campaign", params));
                result.put("utmContent", getParamValue("utm_content", params));
            }
        } catch (Exception ex) {
            LOGGER.error("Could not parse url to utm mapping: ", ex);
        }
        return result;
    }

    public static String getParamValue(String name, Map<String, List<String>> params) {
        return getParamValue(name, params, "");
    }

    public static String getParamValue(String name, Map<String, List<String>> params, String defaultVal) {
        List<String> vals = params.get(name);
        if (vals != null) {
            if (vals.size() > 0) {
                return vals.get(0);
            }
        }
        return defaultVal;
    }

    public static String writeValueAsString(Object o) throws IOException {
        return mapper.writeValueAsString(o);
    }

    public static <V> V fromJson(String json, Class<V> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static String toJson(Object o, Type type) {
        return gson.toJson(o, type);
    }

    public static String toJson(Object o) {
        return gson.toJson(o);
    }

    public static String toExcludedJson(Object o) {
        return excludeJson.toJson(o);
    }

    public static Gson getGson() {
        return gson;
    }

    public static <V> V fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }

    public static <V> V fromJson(JsonElement json, Class<V> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static double maxValue(double... values) {
        double max = Double.MIN_VALUE;
        for(double value : values) max = Math.max(max, value);
        return max;
    }

    public static int maxValue(int... values) {
        int max = Integer.MIN_VALUE;
        for(int value: values) max = Math.max(max, value);
        return max;
    }

    public static double minValue(double... values) {
        double min = Double.MAX_VALUE;
        for(double value : values) min = Math.min(min, value);
        return min;
    }

    public static int minValue(int... values) {
        int min = Integer.MAX_VALUE;
        for(int value: values) min = Math.min(min, value);
        return min;
    }

    public static List<Long> parseRawToLongList(List<Object> list) {
        List<Long> resultList = new ArrayList();
        for (Object o : list) {
            resultList.add(StringUtil.safeParseLong(o));
        }
        return resultList;
    }

    public static List<Double> parseRawToDoubleList(List<Object> list) {
        List<Double> resultList = new ArrayList();
        for (Object o : list) {
            resultList.add(StringUtil.safeParseDouble(o.toString()));
        }
        return resultList;
    }

    public static List<String> parseRawToStringList(List<Object> list) {
        List<String> resultList = new ArrayList();
        for (Object o : list) {
            resultList.add(o.toString());
        }
        return resultList;
    }

    public static List<Integer> parseRawToIntegerList(List<Object> list) {
        List<Integer> resultList = new ArrayList();
        for (Object o : list) {
            resultList.add(StringUtil.safeParseInt(o));
        }
        return resultList;
    }

    public static boolean saveCookiesToFile(Set<Long> cookies, String fileName) {
        BufferedOutputStream bos = null;
        OutputStream out = null;
        boolean result = false;
        try {
            File outputFile = new File(fileName);
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            byte[] serializeObject = serializeObject(cookies);
            out = Files.newOutputStream(Paths.get(URI.create("file:" + outputFile.getAbsolutePath())),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            bos = new BufferedOutputStream(out);
            bos.write(serializeObject);
            bos.flush();
            result = true;
        } catch (Exception ex) {
            LOGGER.error("Error happened when call saveCookiesToFile with error: ", ex);
            result = false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ex) {
                }
            }
        }
        return result;
    }

    public static Set<Long> readCookiesFromFile(String fileName) {
        InputStream is = null;
        BufferedInputStream bis = null;
        Set<Long> result = new HashSet();
        try {
            //Read
            byte[] bytes = new byte[2048];
            int le = 0;
            is = Files.newInputStream(Paths.get(URI.create("file:" + fileName)), StandardOpenOption.READ);
            bis = new BufferedInputStream(is);
            ByteBuffer bb = ByteBuffer.allocate(bis.available());
            while ((le = bis.read(bytes)) != -1) {
                bb.put(bytes, 0, le);
            }
            result = deserializeBytes(bb.array(), Set.class);
        } catch (Exception ex) {
            LOGGER.error("Error happended when readBitSetFromTempFiles with error: ", ex);
        } finally {
            if (null != bis) {
                try {
                    bis.close();
                } catch (Exception ex) {
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (Exception ex) {
                }
            }
        }
        return result;
    }

    public static <T> T deserializeBytes(byte[] bytes, Class<T> clazz) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream is = new ObjectInputStream(in);
        Object readObject = is.readObject();
        return clazz.cast(readObject);
    }

    public static byte[] serializeObject(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static String removeAccent(String s) {
        if (StringUtil.isNullOrEmpty(s)) {
            return null;
        }
        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < sb.length(); i++) {
            sb.setCharAt(i, removeAccent(sb.charAt(i)));
        }
        return sb.toString();
    }

    public static char removeAccent(char ch) {
        int index = Arrays.binarySearch(SPECIAL_CHARACTERS, ch);
        if (index >= 0) {
            ch = REPLACEMENTS[index];
        }
        return ch;
    }
}
