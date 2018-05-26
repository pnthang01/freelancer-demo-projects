package org.iff.prbot.util;

import java.net.SocketAddress;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Random;

/**
 * Created by thangpham on 25/11/2017.
 */
public class StringUtil2 {

    private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm-ss");

    public static String formatDate(long timestamp) {
        return sdf.format(timestamp);
    }

    public static String[] parseAddress(SocketAddress address) {
        String addressStr = address.toString();
        if (addressStr.startsWith("/")) addressStr = addressStr.replace("/", "");
        String[] tmp = addressStr.split(":");
        return tmp;
    }

    /**
     * Get nodeId by host and port
     *
     * @param host
     * @param port
     * @return
     */
    public static String getHashAddress(String host, int port) {
        return host + ":" + port;
    }

    public static String createRandomCode(int codeLength, String id){
        char[] chars = id.toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < codeLength; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output ;
    }
}
