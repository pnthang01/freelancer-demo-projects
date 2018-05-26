package org.iff.simulator;

import java.io.*;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by thangpham on 12/11/2017.
 */
public class SimulatorStarterOld {

    public static final String DUMMY_TEST_FILE = "./dummy_test.iff";
    public static final int BURST_TIME_MISEC = 60;
    public static final long[] MEAN_INTERVALS = new long[]{30, 35, 40, 45, 50, 55, 60, 65, 70, 75};

    public static void main(String[] args) throws IOException {
        File file = new File(DUMMY_TEST_FILE);
        Random rand = new Random();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        for (int i = 0; i < BURST_TIME_MISEC * 100000; i++) {
            bufferedWriter.write(rand.nextLong() + "\n");
        }
        bufferedWriter.close();
        ExecutorService executor = Executors.newFixedThreadPool(1);
        for (long interval : MEAN_INTERVALS) {
            Future<String> future = executor.submit(new Task());
            try {
                System.out.println("Started at interval: " + interval);
                System.out.println("Job at interval " + interval +
                        future.get(BURST_TIME_MISEC, TimeUnit.MILLISECONDS));
                Thread.sleep(interval);
            } catch (Exception e) {
                future.cancel(true);
                System.out.println("Job at interval " + interval + " has bursted in 60 milliseconds");
            }
        }
        executor.shutdownNow();
        file.delete();
    }

    private static class Task implements Callable<String> {

        public String call() throws Exception {
            BufferedReader reader = new BufferedReader(new FileReader(DUMMY_TEST_FILE));
            while (reader.readLine() != null) {
            }
            return "Finished";
        }
    }
}


