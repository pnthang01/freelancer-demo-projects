package asm.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by cchaitany on 18/09/2017.
 */
public class TaskOneMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2) {
            System.err.println("You must specify an input directory param and output tsv param");
            System.exit(0);
        }
        File inputDir = new File(args[0]);
        if (!inputDir.exists()) {
            System.err.println("Input directory does not exist.");
            System.exit(0);
        }
        File outputTSV = new File(args[1]);
        if (outputTSV.exists()) {
            outputTSV.delete();
        }
        outputTSV.createNewFile();
        FileOutputStream fileWritter = null;
        try {
            fileWritter = new FileOutputStream(outputTSV, true);
            ExecutorService executorService = Executors.newFixedThreadPool(20);
            long start = System.currentTimeMillis();
            for (File cateDir : inputDir.listFiles()) {
                for (File dataFile : cateDir.listFiles()) {
                    executorService.execute(new ReadOneFileUnit(dataFile, fileWritter));
                }
            }
            executorService.shutdown();
            while (!executorService.awaitTermination(1l, TimeUnit.SECONDS)) {
            }
            System.out.println("Takes time: " + (System.currentTimeMillis() - start));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if(null != fileWritter) fileWritter.close();
        }
    }

    static class ReadOneFileUnit implements Runnable {
        private File file;
        private FileOutputStream fileWriter;
        public ReadOneFileUnit(File file, FileOutputStream fileWritter ) {
            this.file = file;
            this.fileWriter = fileWritter;
        }
        public void run() {
            try {
                String category = file.getParentFile().getName();
                System.out.println("Start to clean and process file " + file.getName()
                        + " in category: " + category);
                List<String> contentFile = FileUtil.readAllLines(file);
                Document document = new Document();
                document.setCategory(category);
                StringBuilder sb = new StringBuilder();
                for(String line : contentFile) {
                    if(line.contains(":")) {
                        if(line.startsWith("From:")) {
                            document.setSender(line.substring(6));
                        } else if(line.startsWith("Subject:")) {
                            document.setSubject(line.substring(9));
                        } else if(line.startsWith("Organization:")) {
                            document.setSenderAff(line.substring(14));
                        }
                    } else if(!line.startsWith(">")) {
                        sb.append(line.replaceAll("\\s+", " ")).append(" ");
                    }
                }
                sb.deleteCharAt(sb.length() - 1);
                document.setContent(sb.toString().trim());
                fileWriter.write((document.toString() + "\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
