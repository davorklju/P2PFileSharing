import java.io.IOException;
import java.net.*;

/**
 * User: davor
 * Date: 28/11/12
 * Time: 1:47 PM
 */
public class DirectoryServer {
    public static final int port = 40020;
    public volatile static DatagramSocket serverSocket;
    public static InetAddress host;

    static {
        try {
            host = InetAddress.getLocalHost();
            serverSocket = new DatagramSocket(port, host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static void run() {
        while (true) {
            byte[] readData = new byte[128];
            DatagramPacket pkt = new DatagramPacket(readData,readData.length);
            synchronized (serverSocket) {
                try {
                    serverSocket.receive(pkt);
                } catch (IOException e) {
                    System.out.println("could not read from the socket");
                    continue;
                }
                String sd = new String(pkt.getData());
                SocketAddress sa = pkt.getSocketAddress();
                Thread repley = new Thread(new ServerThread(sd,sa));
                repley.run();
            }
        }
    }

    private static class ServerThread implements Runnable{
        private final String[] body;
        private final String method;
        private final String file;
        private final int length;
        private final SocketAddress remoteHost;

        public ServerThread(String message,SocketAddress remoteHost){
            this.remoteHost = remoteHost;
            String[] tmp = message.split("\n");
            String[] header = tmp[0].split(" ");
            this.method = header[0];
            this.file = header[1];
            this.length = Integer.parseInt(header[2]);
            this.body = new String[tmp.length-1];
            System.arraycopy(tmp,1,body,0,body.length);
        }
        @Override
        public void run(){
            byte[] data = new byte[128];
            DatagramPacket writePacket;
            try {
                writePacket = new DatagramPacket(data,data.length,remoteHost);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            for(String s : body){
                System.out.println(s);
            }
        }
    }
}
