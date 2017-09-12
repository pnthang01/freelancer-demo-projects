package flc.exc.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by thangpham on 06/09/2017.
 */
public class EchoClient {
    private int port;
    private Socket soc;

    public EchoClient(int port) {
        this.port = port;
    }

    public void run() {
        InputStream ins = null;
        OutputStream ons = null;
        Scanner sc = null;
        byte[] buff = new byte[1024];
        try {
            sc = new Scanner(System.in);
            soc = new Socket("localhost", port);
            ins = soc.getInputStream();
            ons = soc.getOutputStream();
            int n = 0;
            while (true) {
                System.out.println("Please input your request:");
                String line = sc.nextLine();
                ons.write(line.getBytes());
                n = ins.read(buff);
                line = new String(buff, 0, n);
                if ("ttl".equals(line)) break;
                System.out.println("Server response: " + line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != ins) try { ins.close(); } catch (IOException ex) {}
            if(null != ons) try { ons.close(); } catch (IOException ex) {}
            try {
                soc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        EchoClient client = new EchoClient(args.length > 0 ? Integer.parseInt(args[0]) : 6969);
        client.run();
    }
}
