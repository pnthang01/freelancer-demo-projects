package asm.data;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by cchaitany on 19/09/2017.
 */
public class FileUtil {

    public static void main(String[] args) {
        //Reduce Array to String.
        String[] array = {"Mohan", "Sohan", "Mahesh"};
        Arrays.stream(array).reduce((x, y) -> x + "," + y)
                .ifPresent(s -> System.out.println("Array to String: " + s));
        //Reduce List to String.
        List<String> list = Arrays.asList("Mohan", "Sohan", "Mahesh");
        long t = list.parallelStream()
                .map(word -> word.equalsIgnoreCase("Mohan"))
                .count();

        System.out.println(t);

        List<Integer> test = Arrays.asList(1, 2, 3, 4);
        System.out.println(test.stream().reduce((x, y) -> x + y).get());

        Map<String, Integer> testMap = new HashMap();
        testMap.put("1", 1);
        testMap.put("2", 2);
        testMap.put("3", 3);
        testMap.put("4", 4);
        testMap.put("5", 5);
        Map.Entry<String, Integer> stringIntegerEntry = testMap.entrySet().stream().max(Map.Entry.comparingByValue()).get();
        System.out.println(stringIntegerEntry.getKey());
    }

    public static List<String> readAllLines(File file) throws IOException {
        if (null == file || !file.isFile()) throw new IOException("Please input a file to process");
        List<String> records = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                records.add(line);
            }
        } catch (Exception ex) {
            System.out.println("Error happened when getUnreadRecords has error");
            ex.printStackTrace();
        } finally {
            if (br != null) try {
                br.close();
            } catch (IOException ex) {
            }
        }
        return records;
    }

    public static List<String> readLines(File file, AtomicLong lastMark, int size) {
        RandomAccessFile randAccess = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        List<String> records = new ArrayList();
        try {
            long fileLength = file.length();
            ByteBuffer bb = ByteBuffer.allocate(1048576);
            byte[] arr = new byte[2048];
            int le;
            randAccess = new RandomAccessFile(file, "r");
            randAccess.seek(lastMark.get());
            fis = new FileInputStream(randAccess.getFD());
            bis = new BufferedInputStream(fis);
            while ((le = bis.read(arr)) != -1) {
                for (int i = 0; i < le; ++i) {
                    if ((arr[i] == 10) || (arr[i] == 13 && arr[i + 1] == 10)) {
                        bb.flip();
                        if (arr[i] == 10) {
                            lastMark.set(lastMark.get() + bb.limit() + 1);
                        } else {
                            lastMark.set(lastMark.get() + bb.limit() + 2);
                            ++i;
                        }
                        records.add(new String(bb.array(), 0, bb.limit(), "UTF-8").trim());
                        bb.clear();
                    } else bb.put(arr[i]);
                }
                if (records.size() >= size || fileLength <= lastMark.get()) break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (bis != null) try {
                bis.close();
            } catch (IOException ex) {
            }
            if (fis != null) try {
                fis.close();
            } catch (IOException ex) {
            }
            if (randAccess != null) try {
                randAccess.close();
            } catch (IOException ex) {
            }
        }
        return records;
    }
}
