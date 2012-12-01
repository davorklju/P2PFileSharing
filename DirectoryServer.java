import java.io.*;
import java.net.*;
import java.util.*;

/**
 * User: davor
 * Date: 28/11/12
 * Time: 1:47 PM
 */
public class DirectoryServer extends Thread implements Runnable {

    private static DatagramSocket serverSocket;
    final static int port = 40020;
    private static InetAddress host;
    private static boolean running;
    private static Object lock;
    private static Hashtable<String, P2PFile> table;

    static {
        try {
            host = InetAddress.getLocalHost();
            serverSocket = new DatagramSocket(port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runDirectoryServer() throws IOException {
        System.out.println("the server is running on computer: " + host.getHostName());
        ByteArrayOutputStream msg = new ByteArrayOutputStream();
        while (running) {
            synchronized (lock) {
                byte[] pkt = new byte[128];
                do {
                    DatagramPacket p = new DatagramPacket(pkt, pkt.length);
                    serverSocket.receive(p);
                    msg.write(pkt,1,pkt.length-1);
                } while (pkt[0] == 1);
                String message = new String(msg.toByteArray());
                doMethod(message, ""/*TODO get the src ip*/);
            }
        }
        serverSocket.close();
    }

    private static void doMethod(String message, String clientIP) throws UnknownHostException {
        byte[] data = null;
        String[] msg = message.split("\n");
        String[] header = msg[0].split(" ");
        String[] body = msg[1].split(" ");

        if (header[0].equals("INFORM")) {
            System.out.println("GOT INFORM");
            String files = "";
            for (String s : table.keySet()) {
                files += s + "\n";
            }
            ByteArrayOutputStream[] bos = Message.mkPackets("0.0 200 OK", files);
            for (ByteArrayOutputStream b : bos) {
                DatagramPacket pk = new DatagramPacket(b.toByteArray(), 128, InetAddress.getByName(clientIP), 40020);
                try {
                    serverSocket.send(pk);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (header[0].equals("DOWNLOAD")) {
            System.out.println("GOT DOWNLOAD");
        } else if (header[0].equals("RATE")) {
            System.out.println("GOT RATE");
            P2PFile f = table.get(header[1]);
            double r = Double.parseDouble(header[2]);
            f.rate(r);
        } else if (header[0].equals("EXIT")) {
            running = false;
            System.out.println("GOT EXIT");
        } else {
            System.out.println("Did not understand message: " + header[0]);
        }
    }
}
