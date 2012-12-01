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

    public static void main(String[] args) {
        P2PClient p = new P2PClient();
        p.run();
    }

    public P2PClient() {
        port = Port.port;
        Scanner in = new Scanner(System.in);
        String strHost = in.nextLine();
        try {
            host = InetAddress.getByName(strHost);
        } catch (UnknownHostException e) {
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
        File f = new File(fileName);
        long size = f.length();
        String msg = fileName + " " + size + "\n";
        Scanner inFile = new Scanner(fileName);
        while (inFile.hasNextLine())
            msg += inFile.nextLine();
        msg += "\n";
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        System.out.println("finished");
        datagramSocket.close();
    }

    private void sendMessage(String msg) throws IOException {
        if(datagramSocket == null)
            datagramSocket = new DatagramSocket();
        byte[] data = new byte[128];
        byte[] msgData = msg.getBytes();
        DatagramPacket pkt = new DatagramPacket(data,data.length,host,Port.port);
        int x = 0;
        for(int i=0,len;i<msgData.length;i+=len){
            System.out.println(x++);
            len = msgData.length - i > 127 ? 127 : msgData.length - i;
            data[0] = (byte) (i + len >= msgData.length ? 1 : 0);
            System.arraycopy(msgData,i,data,1,len);
            datagramSocket.send(pkt);
        }
    }
}
