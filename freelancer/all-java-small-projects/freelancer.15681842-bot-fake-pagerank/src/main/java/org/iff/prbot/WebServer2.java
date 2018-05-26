package org.iff.prbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by thangpham on 26/11/2017.
 */
public class WebServer2 extends Thread {

    private final ServerSocket webServer;
    private final String host;
    private final int port;
    private final String fakeUrl;
    private AtomicBoolean run = new AtomicBoolean(true);

    //"http://everythingfromhere.isfake/this-is-an-assigment.html"
    public WebServer2(String host, int port, String fakeUrl) throws IOException {
        this.webServer = new ServerSocket(port, 100, InetAddress.getByName(host));
        System.out.println("Started web-server at " + host + ":" + port + " contains fake-url: " + fakeUrl);
        this.host = host;
        this.port = port;
        this.fakeUrl = fakeUrl;
    }

    public void close() throws IOException, InterruptedException {
        run.set(false);
        Thread.sleep(500);
        webServer.close();
    }

    @Override
    public void run() {
        System.out.println("Waiting for connection");
        while (run.get()) {
            try {
                // wait for a connection
                Socket client = webServer.accept();
                // remote is now the connected socket
                System.out.println("Connection, sending data.");
                PrintWriter out = new PrintWriter(client.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                Map<String, String> requestHeaders = getRequestHeaders(in);
                String hostContext = hostConext(host, port);
                String requestPath = requestHeaders.get("Path").replaceAll(hostContext, "");
                requestController(out, hostContext, requestPath, fakeUrl);
                out.flush();
                client.close();
            } catch (Exception e) {
                System.err.println("Error: " + e);
                e.printStackTrace();
            }
        }
    }

    private static void requestController(PrintWriter out, String hostContext, String requestPath, String fakeUrl) {
        out.print(buildResponseHeader());
        out.print(headerPage());
        if ("/".equals(requestPath) || "".equals(requestPath)) out.print(mainBodyPage(hostContext));
        else if ("/apple-failed.html".equals(requestPath)) out.println(appleFailedBodyPage(fakeUrl));
        else if ("/google-failed.html".equals(requestPath)) out.println(googleFailedBodyPage(fakeUrl));
        else out.println("404");
    }

    private static String appleFailedBodyPage(String fakeUrl) {
        String rs = "<body>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Most people lost for this simple question - Apple</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! People should not do this because it's harmful - Apple</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Be careful with your doctors said - Apple</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! After see this, you won't believe in government anymore - Apple</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Why you are still poor? This is why! - Apple</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Stop using your phone, you're observed by deepweb - Apple</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! You are not ugly, you are awesome! - Apple</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Earth will be destroyed in 100 years - Apple</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Stop watching porn, here is why! - Apple</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Stop masturbating, here is why! - Apple</a></h1>" +
                "</body>" +
                "</html>";
        return rs.replaceAll("\\$\\{fakeUrl\\}", fakeUrl);
    }

    private static String googleFailedBodyPage(String fakeUrl) {
        String rs = "<body>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Most people lost for this simple question - Google</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! People should not do this because it's harmful - Google</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Be careful with your doctors said - Google</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! After see this, you won't believe in government anymore - Google</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Why you are still poor? This is why! - Google</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Stop using your phone, you're observed by deepweb - Google</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! You are not ugly, you are awesome! - Google</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Earth will be destroyed in 100 years - Google</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Stop watching porn, here is why! - Google</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Stop masturbating, here is why! - Google</a></h1>" +
                "</body>" +
                "</html>";
        return rs.replaceAll("\\$\\{fakeUrl\\}", fakeUrl);
    }

    private static String mainBodyPage(String hostContext) {
        String rs = "<body><ul>" +
                "<li><h1><a href=\"${hostContext}/apple-failed.html\">Apple is madness</a></h1></li>" +
                "<li><h1><a href=\"${hostContext}/google-failed.html\">Google is painful</a></h1></li>" +
//                "<h1><a href=\"${hostContext}/facebook-failed.html\">Facebook is doom</a></h1>" +
//                "<h1><a href=\"${hostContext}/amazon-failed.html\">Amazon is over</a></h1>" +
//                "<h1><a href=\"${hostContext}/alibaba-failed.html\">Alibaba is crazy</a></h1>" +
                "</ul></body>" +
                "</html>";
        return rs.replaceAll("\\$\\{hostContext\\}", hostContext);
    }

    private static String hostConext(String host, int port) {
        return "http://" + host + ":" + port;
    }

    private static String headerPage() {
        return "<html>" +
                "<head>" +
                "<title>Check this out</title>" +
                "</head>";
    }

    private static String buildResponseHeader() {
        return "HTTP/1.0 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n";
    }

    private static Map<String, String> getRequestHeaders(BufferedReader in) throws IOException {
        String str = null;
        Map<String, String> result = new HashMap();
        boolean firstLine = true;
        while (!(str = in.readLine()).equals("")) {
            String[] split = null;
            if (firstLine) {
                split = str.split(" ");
                result.put("Method", split[0]);
                result.put("Path", split[1]);
                result.put("Protocol", split[2]);
                firstLine = false;
            } else {
                split = str.split(": ");
                result.put(split[0], split[1]);
            }
        }
        return result;
    }
}
