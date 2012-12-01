import java.io.*;
import java.net.*;
import java.util.*;

/**
 * User: davor
 * Date: 29/11/12
 * Time: 1:49 PM
 */
public class P2PServer extends Thread {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    final static int port = 40020;
    private static InetAddress host;
    private static boolean running;

    public P2PServer(ServerSocket socket) {
        this.serverSocket = socket;
        host = socket.getInetAddress();
        running = true;
    }

    public void runP2PServer() throws IOException {
        try {
            serverSocket = new ServerSocket(port);

        } catch (Exception e) {
            e.printStackTrace();
        }
        while (running) {
            try {
                ClientToClient c2c = clientReceiver(serverSocket);
                Thread t = new Thread(c2c);
                t.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ClientToClient clientReceiver(ServerSocket socket) throws IOException {
        return new ClientToClient(socket.accept());
    }


    public class ClientToClient implements Runnable {
        private Socket clientSocket;
        private Scanner scan;
        private PrintWriter write;

        public ClientToClient(Socket socket) {
            clientSocket = socket;
        }

        public void run() {
            try {
                scan = new Scanner(clientSocket.getInputStream());
                write = new PrintWriter(clientSocket.getOutputStream());
                String[] inMsg = scan.nextLine().split("\n");
                String file = inMsg[0].split(" ")[1];
                Scanner fileReader = new Scanner(file);
                String body = "";
                while(fileReader.hasNextLine()){
                    body += fileReader.nextLine() + "\n";
                }
                String outMsg = "0.0 200 OK\n" + body;
                write.println(outMsg);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    scan.close();
                    write.close();
                    clientSocket.close();
                } catch (Exception e) {
                    System.out.println("Couldn't close I/O streams");
                }
            }
        }
    }
}
