import java.net.*;
import java.util.*;

/**
 * User: davor
 * Date: 29/11/12
 * Time: 12:35 PM
 */
public class P2PClient {
    private boolean running;
    private InetAddress host;
    private InetSocketAddress server;
    private volatile ServerSocket inSocket;
    private DatagramSocket outSock;

    public static void main(String args[]) {

    }

    public P2PClient() throws UnknownHostException {
        this.running = true;
        host = InetAddress.getLocalHost();
        Scanner in = new Scanner(System.in);
        System.out.println("Enter a server to connect to: ");
        String serverAddr = in.nextLine();
        server = new InetSocketAddress(serverAddr, 40020);
    }

    public void run() {
        Scanner in = new Scanner(System.in);
        while (running) {
            System.out.println("enter a number");
            System.out.println("1: inform and update");
            System.out.println("2: download");
            System.out.println("3: rate");
            System.out.println("4: exit");
            int input = in.nextInt();
            try {
                sendDatagram(input);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    public int sendDatagram(int msg) throws SocketException {
        Scanner in = new Scanner(System.in);

        byte[] data = null;
        DatagramPacket pack;

        switch (msg) {
            case 1: {
                System.out.println("enter a file name");
                String input = in.nextLine();
                data = input.getBytes();
                break;
            }
            case 2: {
                /*
                * TODO get list of files from server and print them
                * */
                System.out.println("select a file name");
                String input = in.nextLine();
                break;
            }
            case 3: {
                /*
                * TODO get list of files from server and print them
                * */
                System.out.println("select a file name");
                String input = in.nextLine();
                System.out.println("enter a rating from 0 to 5");
                double r = in.nextDouble();
                /*
                *TODO rate the file
                 */
                break;
            }
            case 4:
                this.running = false;
                break;
            default:
                System.out.println("did not understand option: " + msg);
                msg = 0;
        }
        return msg;
    }
}
