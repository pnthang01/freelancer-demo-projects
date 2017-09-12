package com.ants.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Modifier;

/**
 * Created by thangpham on 08/09/2017.
 */
public class MethodUtil {

    private static final Gson gson = new Gson();
    private static final Gson excludeJson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
            .create();

    public static double maxValue(double... values) {
        double max = Double.MIN_VALUE;
        for (double value : values) max = Math.max(max, value);
        return max;
    }

    public static int maxValue(int... values) {
        int max = Integer.MIN_VALUE;
        for (int value : values) max = Math.max(max, value);
        return max;
    }

    public static double minValue(double... values) {
        double min = Double.MAX_VALUE;
        for (double value : values) min = Math.min(min, value);
        return min;
    }

    public static int minValue(int... values) {
        int min = Integer.MAX_VALUE;
        for (int value : values) min = Math.min(min, value);
        return min;
    }

    public static long minValue(long... values) {
        long min = Long.MAX_VALUE;
        for (long value : values) min = Math.min(min, value);
        return min;
    }

    public static long maxValue(long... values) {
        long max = Long.MIN_VALUE;
        for (long value : values) max = Math.max(max, value);
        return max;
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
}
