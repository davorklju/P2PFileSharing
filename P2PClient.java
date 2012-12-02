import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

/**
 * User: davor
 * Date: 29/11/12
 * Time: 12:35 PM
 */
public class P2PClient {
    private final int port;
    private InetAddress host;
    private volatile DatagramSocket datagramSocket;
    private String path;


    public static void main(String[] args) {
        P2PClient p = new P2PClient();
        p.run();
    }

    public P2PClient() {
        port = Port.port;
        Scanner in = new Scanner(System.in);
        String strHost = in.nextLine();
        path = "P2PFiles/";
        try {
            host = InetAddress.getByName(strHost);
            datagramSocket = new DatagramSocket();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public int getMessage() {
        System.out.println("Select an option");
        System.out.println("1) Inform and update");
        System.out.println("2) Query");
        System.out.println("3) Rate");
        System.out.println("4) exit");
        Scanner in = new Scanner(System.in);
        int msg = in.nextInt();
        return msg;
    }

    public String getFileForUpload() {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter the name of file you wish to upload");
        String fileName = in.nextLine();
        File f = new File(path+fileName);
        if(!f.exists())
            System.out.println("cant find file" + f.getAbsolutePath());
        long size = f.length();
        String msg = fileName + " " + size + "\n";
        return msg;
    }

    public String getFileRating() {
        Scanner in = new Scanner(System.in);
        System.out.println("Which file do you want to Rate");
        String fileName = in.nextLine();
        System.out.println("Enter a rating from 0 to 5");
        String rating = in.nextDouble() + "";
        return fileName + " " + rating.length() + " 0\n" + rating + "\n";

    }

    public String getFileForQuery() {
        Scanner in = new Scanner(System.in);
        System.out.println("which file do you want to download");
        String fileName = "";
        while (!fileName.contains("@")) {
            System.out.println("enter file@host");
            fileName = in.nextLine();
        }
        return fileName + " " + "0\n";
    }

    public void run() {
        boolean running = true;
        while (running) {
            String msg = "";
            switch (getMessage()) {
                case 1:
                    msg += "INFORM ";
                    msg += getFileForUpload();
                    break;
                case 2:
                    msg += "QUERY ";
                    msg += getFileForQuery();
                    break;
                case 3:
                    msg += "RATE ";
                    msg += getFileRating();
                    break;
                case 4:
                    running = false;
                    break;
            }
            if(running)
                try {
                    sendMessage(msg);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String inMsg = readMessage();
                    printAck(inMsg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        System.out.println("finished");
        datagramSocket.close();
    }

    private void printAck(String inMsg) {

    }

    private String readMessage() throws IOException {
        byte[] data = new byte[128];
        byte[] inData = null;
        DatagramPacket pkt = new DatagramPacket(data,data.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int x=0;
        do {
            System.out.println(x++);
            datagramSocket.receive(pkt);
            System.out.println("got this far");
            inData = pkt.getData();
            baos.write(inData,1,inData.length-1);
        }while (inData[0] != 1);
        System.out.println("finished reading");
        return new String(baos.toByteArray());
    }

    private void sendMessage(String msg) throws IOException {
        byte[] data = new byte[128];
        byte[] msgData = msg.getBytes();
        DatagramPacket pkt = new DatagramPacket(data,data.length,host,Port.port);
        for(int i=0,len;i<msgData.length;i+=len){
            len = msgData.length - i > 127 ? 127 : msgData.length - i;
            data[0] = (byte) (i + len >= msgData.length ? 1 : 0);
            System.arraycopy(msgData,i,data,1,len);
            datagramSocket.send(pkt);
        }
    }
}
