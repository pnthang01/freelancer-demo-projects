package flc.exc.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by thangpham on 06/09/2017.
 */
public class EchoServer {
    private int port;
    private boolean stopped;
    private ServerSocket server;

    public EchoServer(int port) {
        this.port = port;
        stopped = false;
    }

    public void start() {
        try {
            server = new ServerSocket(port);
            System.out.println("EchoServer started at " + port);
            while (!stopped) new EchoService(server.accept()).start();
        } catch (Exception ex) {

        }
    }

    public class EchoService extends Thread {
        private Socket soc;
        private String host;
        private int port;

        public EchoService(Socket soc) {
            this.soc = soc;
            host = soc.getInetAddress().getHostAddress();
            port = soc.getPort();
        }

        public void run() {
            System.out.println("Client at " + host + ":" + port + " connected");
            InputStream ins = null;
            OutputStream ons = null;
            byte[] buff = new byte[1024];
            try {
                ins = soc.getInputStream();
                ons = soc.getOutputStream();
                int n = 0;
                while ((n = ins.read(buff)) != -1) {
                    String line = new String(buff, 0, n);
                    System.out.println("Received message " + line + " at " + host + ":" + port + " connected");
                    ons.write(line.getBytes());
                    if ("ttl".equals(line)) break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (null != ins) try { ins.close(); } catch (IOException e) {}
                if (null != ons) try { ons.close(); } catch (IOException e) {}
                try {
                    soc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        EchoServer server = new EchoServer(args.length > 0 ? Integer.parseInt(args[0]) : 6969);
        server.start();
    }


}
