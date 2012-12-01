import java.io.ByteArrayOutputStream;

/**
 * User: davor
 * Date: 29/11/12
 * Time: 3:53 PM
 */
public class Message {

    public static byte[] getMData(byte[] data){
        byte[] mdata = new byte[8];
        System.arraycopy(data,0,mdata,0,8);
        return mdata;
    }

    public static String getData(byte[] data){
        byte[] mdata = getMData(data);
        int size = Integer.parseInt(new String(mdata).substring(5,6));
        byte[] body = new byte[size];
        System.arraycopy(data,8,body,0,size);
        return new String(body);
    }

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
