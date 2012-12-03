import java.io.*;
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
    private volatile ServerSocket serverSocket;
    private String path;
    boolean running;

    public static void main(String[] args) {
        P2PClient p = new P2PClient();
        p.run();
    }

    public P2PClient() {
        port = Port.port;
        System.out.println("enter the name of the server");
        Scanner in = new Scanner(System.in);
        String strHost = in.nextLine();
        path = "P2PFiles/";
        try {
            host = InetAddress.getByName(strHost);
            datagramSocket = new DatagramSocket();
            serverSocket = new ServerSocket(Port.port + 1);
            Thread t = new Thread(new ServerThread());
            t.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        File f = new File(path + fileName);
        if (!f.exists())
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
        boolean download = false;
        running = true;
        while (running) {
            String inMsg;
            String msg = "";
            switch (getMessage()) {
                case 1:
                    msg += "INFORM ";
                    msg += getFileForUpload();
                    break;
                case 2:
                    msg += "QUERY #LIST# 0\n";
                    try {
                        sendMessage(msg);
                        inMsg = readMessage();
                        printAck(inMsg);
                        msg = "QUERY ";
                        msg += getFileForQuery();
                        download = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    msg += "QUERY #LIST# 0\n";
                    try {
                        sendMessage(msg);
                        inMsg = readMessage();
                        printAck(inMsg);
                        msg = "RATE ";
                        msg += getFileRating();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    running = false;
                    break;
                default:
                    System.out.println("select a correct input");
                    continue;
            }
            if (running)
                try {
                    sendMessage(msg);
                    inMsg = readMessage();
                    printAck(inMsg);
                    if (download)
                        startDownload(msg, inMsg);
                    download = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        System.out.println("finished");
        datagramSocket.close();
    }

    private void startDownload(String msg, String inMsg) {
        String file = msg.split("\n")[0].split(" ")[1];
        boolean ok = inMsg.split("\n")[0].split(" ")[1].equals("200");
        if (ok) {
            Thread t = new Thread(new ClientThread(file));
            t.start();
        }
    }

    private void printAck(String inMsg) {
        String[] msg = inMsg.split("\n");
        String[] stat = msg[0].split(" ");
        if (stat[1].equals("200")) {
            String[] header = msg[1].split(" ");
            int len = Integer.parseInt(header[1]);
            if (len == 0)
                System.out.print("200 OK");
            String files = msg[2];
            System.out.println(files);
        } else {
            System.out.println(msg[0]);
        }
    }

    private String readMessage() throws IOException {
        byte[] data = new byte[128];
        byte[] inData = null;
        DatagramPacket pkt = new DatagramPacket(data, data.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int x = 0;
        do {
            System.out.println(x++);
            datagramSocket.receive(pkt);
            inData = pkt.getData();
            baos.write(inData, 1, inData.length - 1);
        } while (inData[0] != 1);
        System.out.println("finished reading");
        return new String(baos.toByteArray());
    }

    private void sendMessage(String msg) throws IOException {
        byte[] data = new byte[128];
        byte[] msgData = msg.getBytes();
        DatagramPacket pkt = new DatagramPacket(data, data.length, host, Port.port);
        for (int i = 0, len; i < msgData.length; i += len) {
            len = msgData.length - i > 127 ? 127 : msgData.length - i;
            data[0] = (byte) (i + len >= msgData.length ? 1 : 0);
            System.arraycopy(msgData, i, data, 1, len);
            datagramSocket.send(pkt);
        }
    }

    private class ServerThread implements Runnable {

        private void mkMessage(String file, OutputStream outputStream) {
            System.out.println(path + file);
            File f = new File(path + file);

            DataOutputStream out = new DataOutputStream(outputStream);
            FileInputStream in;
            try {
                in = new FileInputStream(f);
                int data;
                while((data = in.read()) != -1)
                    out.write(data);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (P2PClient.this.running) {
                Socket tmp = null;
                DataOutputStream out = null;
                BufferedReader in = null;
                try {
                    tmp = serverSocket.accept();

                    in = new BufferedReader(new InputStreamReader(tmp.getInputStream()));

                    String file = in.readLine().split(" ")[1];

                    mkMessage(file, tmp.getOutputStream());

                    tmp.close();
                    System.out.println("finished writing");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ClientThread implements Runnable {
        private String file;
        private InetAddress host;

        public ClientThread(String file) {
            System.out.println("got to thread");
            try {
                String[] fh = file.split("@");
                this.file = fh[0];
                System.out.println("the file i want to download is " + file);
                host = InetAddress.getByName(fh[1]);
                System.out.println("the host i want to download from is " + host.getHostName());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean running = true;
            Socket socket = null;
            DataOutputStream out = null;
            BufferedReader in = null;
            try {
                socket = new Socket(host, Port.port + 1);
                out = new DataOutputStream(socket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.writeBytes("GET " + file + " 0\n");
                out.flush();

                mkFile(socket.getInputStream());

                System.out.println("finished reading");

                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void mkFile(InputStream inputStream) {

            File f = new File(path+"new_"+file);
            System.out.println(f.getAbsolutePath());
            if(!f.exists())
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            DataInputStream in = new DataInputStream(inputStream);
            FileOutputStream out;
            try {
                out = new FileOutputStream(f);
                int data;
                while((data = in.read()) != -1)
                    out.write(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*
            BufferedImage img;
            try {
                img = ImageIO.read(ImageIO.createImageInputStream(inputStream));
                ImageIO.write(img,"JPG",f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        }
    }
}
