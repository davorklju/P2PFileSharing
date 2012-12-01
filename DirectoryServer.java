import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Hashtable;

/**
 * User: davor
 * Date: 28/11/12
 * Time: 1:47 PM
 */
public class DirectoryServer {
    public static final int port = Port.port;
    public volatile static DatagramSocket serverSocket;
    public static InetAddress host;
    public static Hashtable<String,P2PFile> table;

    public static void main(String[] args) {
        DirectoryServer.run();
    }

    static {
        try {
            host = InetAddress.getLocalHost();
            System.out.println(host.getHostName());
            serverSocket = new DatagramSocket(port, host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static synchronized String getPacket(DatagramPacket pkt,byte[] data) throws IOException {
        ByteArrayOutputStream msg = new ByteArrayOutputStream();
        byte[] inData = null;
        do{
            serverSocket.receive(pkt);
            inData = pkt.getData();
            msg.write(inData, 1, inData.length-1);
        }while (inData[0] != 1);
        System.out.println("Got this far C");
        return new String(msg.toByteArray());
    }

    public static void run() {
        while (true) {
            byte[] readData = new byte[128];
            DatagramPacket pkt = null;
            String message = null;
            byte[] data = null;
            try {
                data = new byte[128];
                pkt = new DatagramPacket(data,data.length);
                System.out.println("Got this far 1");
                message = getPacket(pkt,data);
                System.out.println("Got this far before PKT");
                ServerThread st = new ServerThread(message,pkt.getAddress());
                System.out.println("Got this far after PKT");
                Thread thread = new Thread(st);
                thread.run();
                System.out.println("Got this far 2");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ServerThread implements Runnable{
        private final String[] body;
        private final String method;
        private final String file;
        private final int length;
        private final InetAddress remoteHost;

        public ServerThread(String message,InetAddress remoteHost){
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
            if(method.equals("INFORM")){
                informAndUpdate();
            }
            else if(method.equals("QUERY")){
                query();
            }
            else if(method.equals("RATE")){
                rate();
            }
            else {
                System.out.println("did not understand " + method);
            }
        }

        private void rate() {
            System.out.println("got Rate");
        }

        private void query() {
            System.out.println("got QUERY");
            System.out.println(method+" "+file+"@"+remoteHost.getHostName()+" "+length);
        }

        private void informAndUpdate(){
            System.out.println("got INFORM");
            System.out.println(method + " " + file + " " + length);
        }
    }
}
