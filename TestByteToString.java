import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TestByteToString {
    public static void main(String[] args) throws IOException {
        String s = "hello world";
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write(s.getBytes(), 2, s.getBytes().length - 2);
        System.out.println(new String(b.toByteArray()));
    }
}