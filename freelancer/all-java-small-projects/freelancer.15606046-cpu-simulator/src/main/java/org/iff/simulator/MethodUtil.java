package org.iff.simulator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by thangpham on 12/11/2017.
 */
public class MethodUtil {

    public static DecimalFormat nf = new DecimalFormat("0.00000");
    public static DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    public static void logInfo(String text) {
        Date curr = new Date();
        System.out.println(String.format("[%s] %s", df.format(curr), text));
    }

    public static void writeDataToFile(String fileName, String data) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if(null != fileWriter) fileWriter.close();
        }
    }

}
