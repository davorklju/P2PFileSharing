import java.io.ByteArrayOutputStream;

/**
 * User: davor
 * Date: 29/11/12
 * Time: 3:53 PM
 */
public class Message {

    public static ByteArrayOutputStream[] mkPackets(String header, String body) {
        String msg = header + "\n" + body;
        byte[] msgBytes = msg.getBytes();
        ByteArrayOutputStream[] packets = new ByteArrayOutputStream[msg.length()/128];
        for(int i=0;i<msgBytes.length;i++){
            int offset = msgBytes.length >= 128 ? 128 : 128 - msgBytes.length;
            packets[i].write(i + 1 < msgBytes.length ? 1 : 0);
            packets[i].write(msgBytes,i,offset);
        }
        return packets;
    }
}
