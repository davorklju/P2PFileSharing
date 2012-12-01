import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;

/**
 * User: davor
 * Date: 29/11/12
 * Time: 12:35 PM
 */
public class P2PClient {
    private final int port;
    private final InetSocketAddress host;

    public P2PClient(){
        port = 40020;
        Scanner in = new Scanner(System.in);
        String shost = in.nextLine();
        host = new InetSocketAddress(shost,port);
    }

    public void run(){
        DatagramSocket writeSocket = null;
        try {
            writeSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        String msg = "INFORM AFILE.JPEG 0\n";
        byte[] data = msg.getBytes();
        DatagramPacket pkt = null;
        try {
            pkt = new DatagramPacket(data,data.length,host);
            writeSocket.send(pkt);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
