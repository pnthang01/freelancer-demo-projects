import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by thangpham on 09/09/2017.
 */
public class UrlCheckerMainClass {

    /**
     * This method will populate data and make data size by your choice, default is 100k urls
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        UrlCheckerMainClass checkerTest = new UrlCheckerMainClass();
        List<String> urlList = checkerTest.populateTestData(100000);
        long startTime = System.currentTimeMillis();
        checkerTest.checkUrls(urlList);
        System.out.println("Url checking takes " + (System.currentTimeMillis() - startTime) + " miliseconds");
    }

    /**
     * You can put your exist urls into this method
     *
     * @return
     */
    public List<String> existUrlList() {
        List<String> rs = new ArrayList();
        rs.add("http://google.com");
        rs.add("http://facebook.com");
        rs.add("http://www.idph.net/conteudos/ebooks/republic.pdf");
        rs.add("http://infolab.stanford.edu/~ullman/mmds/book.pdf");
        rs.add("http://contentserver.adobe.com/store/books/HuckFinn.pdf");
        rs.add("http://greenteapress.com/thinkpython/thinkpython.pdf");
        return rs;
    }

    /**
     * Populate exist urls with fake urls to make massive data
     *
     * @param size
     * @return
     */
    public List<String> populateTestData(int size) {
        Random rand = new Random();
        List<String> rs = new ArrayList();
        for (int i = 1; i <= size; i++) rs.add(String.format("www.test.com/test-XYZ_%05d.odf", i));
        for (String url : existUrlList()) rs.set(rand.nextInt(size), url);
        return rs;
    }

    public void checkUrls(List<String> urls) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        for (String url : urls) executorService.execute(new UrlChecker(url));
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {}
    }


    class UrlChecker implements Runnable {
        String url;

        public UrlChecker(String url) {
            this.url = url;
        }

        public void run() {
            try {
                new URL(url).openConnection();
                System.out.println("Url exist:" + url);
            } catch (IOException e) {
            }
        }
    }
}
