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
            serverSocket = new DatagramSocket(Port.port);
            table = new Hashtable<String, P2PFile>();
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
                message = getPacket(pkt,data);
                ServerThread st = new ServerThread(message,pkt.getAddress(),pkt.getPort());
                Thread thread = new Thread(st);
                thread.run();
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
        private final int port;
        private final InetAddress remoteHost;

        public ServerThread(String message,InetAddress remoteHost, int port){
            this.port = port;
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

        private void sendMessage(String msg) throws IOException {
            byte[] data = new byte[128];
            byte[] msgData = msg.getBytes();

            DatagramPacket pkt = new DatagramPacket(data,data.length,remoteHost,port);

            int x = 0;
            for(int i=0,len;i<msgData.length;i+=len){
                System.out.println(x++);
                len = msgData.length - i > 127 ? 127 : msgData.length - i;
                data[0] = (byte) (i + len >= msgData.length ? 1 : 0);
                System.arraycopy(msgData,i,data,1,len);
                serverSocket.send(pkt);
            }
            System.out.println("finished sending");
        }

        private void query() {
            System.out.println("got QUERY");
            System.out.println(method+" "+file+"@"+remoteHost.getHostName()+" "+length);
            String key = file + "@" + remoteHost.getHostName();
            for(String s : table.keySet())
                System.out.println(s);
            String msg = "";
            if(table.contains(key)){
               msg += "1.0 200 OK\n";
               msg += "content-length " + 0 + "\n";
            }
            else{
                msg += "1.0 400 ERROR\n";
            }
            try {
                sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void rate() {
            System.out.println("got Rate");
            String key = file + "@" + remoteHost.getHostName();
            String msg = "";
            if(table.contains(key)){
                P2PFile f = (P2PFile)table.get(key);
                msg += "1.0 200 OK\n";
                msg += "content-length " + 0 + "\n";
            }
            else{
                msg += "1.0 400 ERROR\n";
            }
            try {
                sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void informAndUpdate(){
            System.out.println("got INFORM");
            System.out.println(method + " " + file + " " + length);
            String key = file + "@" + remoteHost.getHostName();
            String msg = "";
            if(!table.contains(key)){
                table.put(key,new P2PFile(file,remoteHost.getHostName()));
                msg += "1.0 200 OK\n";
                msg += "content-length " + 0 + "\n";
            }
            else {
                msg += "1.0 201 ALREADY_HAVE\n";
            }
            try {
                sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
