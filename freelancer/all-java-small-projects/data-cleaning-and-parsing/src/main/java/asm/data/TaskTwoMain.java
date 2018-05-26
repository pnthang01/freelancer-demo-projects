package asm.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by cchaitany on 19/09/2017.
 */
public class TaskTwoMain {

    private static ConcurrentMap<String, CopyOnWriteArrayList<DocumentAggregation>> aggMap =
            new ConcurrentHashMap();

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("You must specify an input tsv param and output tsv param");
            System.exit(0);
        }
        File inputTsv = new File(args[0]);
        if (!inputTsv.exists()) {
            System.err.println("Input tsv does not exist.");
            System.exit(0);
        }
        File outputTSV = new File(args[1]);
        if (outputTSV.exists()) {
            outputTSV.delete();
        }
        outputTSV.createNewFile();
        FileOutputStream fileWritter = null;
        int parallelSize = 300;
        try {
            fileWritter = new FileOutputStream(outputTSV);
            AtomicLong lastMark = new AtomicLong(0);
            ExecutorService executorService = Executors.newFixedThreadPool(20);
            List<String> lines = null;
            long totalDocs = 0;
            while ((lines = FileUtil.readLines(inputTsv, lastMark, parallelSize)).size() > 0) {
                totalDocs += lines.size();
                executorService.execute(new ReadTwoFileUnit(lines));
            }
            executorService.shutdown();
            while (!executorService.awaitTermination(1l, TimeUnit.SECONDS)) {
            }
            AtomicLong totalBody = new AtomicLong(0);
            Map<String, Double> avgBodyPerCate = new HashMap<>();
            aggMap.forEach((cate, list) -> {
                int cateSize = list.size();
//                Object[] longStream = list.stream().map(dogAgg -> dogAgg.getBodyWordCount()).toArray();
//                for (Object o : longStream) System.out.println(cate + ":" + o);
                long cateBody = list.parallelStream().map(docAgg -> docAgg.getBodyWordCount())
                        .reduce((x, y) -> x + y).get();
                totalBody.addAndGet(cateBody);
                avgBodyPerCate.put(cate, (double) cateBody / cateSize);
            });
            String maxCate =
                    avgBodyPerCate.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
            String minCate = avgBodyPerCate.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();
            //
            fileWritter.write((totalDocs + "\t" + (double) totalBody.get() / totalDocs
                    + "\t" + (double) totalDocs / aggMap.size() + "\t" + maxCate + "\t" + minCate).getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != fileWritter) fileWritter.close();
        }
    }

    static class ReadTwoFileUnit implements Runnable {
        private List<String> inputRecords;

        public ReadTwoFileUnit(List<String> inputRecords) {
            this.inputRecords = inputRecords;
        }

        public void run() {
            for (String line : inputRecords) {
                try {
                    String[] splitTemp = line.split("\t");
                    String cate = splitTemp[0];
                    CopyOnWriteArrayList<DocumentAggregation> docAggList = aggMap.get(cate);
                    if (null == docAggList) {
                        docAggList = new CopyOnWriteArrayList<DocumentAggregation>();
                        CopyOnWriteArrayList<DocumentAggregation> temp = aggMap.putIfAbsent(cate, docAggList);
                        docAggList = null == temp ? docAggList : temp;
                    }
                    //
                    long bodyCount = 0;
                    if (splitTemp.length >= 5)
                        bodyCount = Arrays.stream(splitTemp[4].split(" ")).count();
                    DocumentAggregation docAgg = new DocumentAggregation();
                    docAgg.setCategory(cate);
                    docAgg.setBodyWordCount(bodyCount);
                    docAggList.add(docAgg);
                } catch (Exception ex) {
                    System.err.println(line);
                    ex.printStackTrace();
                }
            }
        }
    }
}
