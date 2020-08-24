import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Test {
    public static void main(String[] args) throws UnsupportedEncodingException {
        byte[] bytes = {
                60,
                117,
                110,
                107,
                110,
                111,
                119,
                110,
                32,
                115,
                115,
                105,
                100,
                62
        };
        System.out.println(Arrays.toString(stringToBytes("C8:44:0C:81:B0:BB")));
        System.out.println(bytesToString(bytes));
    }
    private static String bytesToString (byte[] bytes) {
        return new String(bytes);
    }
    private static byte[] stringToBytes (String str) throws UnsupportedEncodingException {
        return str.getBytes("UNICODE");
    }
}