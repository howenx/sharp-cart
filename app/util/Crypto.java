package util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by handy on 15/12/17.
 * kakao china
 */
public class Crypto {

    public static String md5(String message) {
        String digest = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(message.getBytes("UTF-8"));
            byte[] hashedBytes = md.digest();

            StringBuilder stringBuffer = new StringBuilder();
            for (byte b : hashedBytes) {
                stringBuffer.append(String.format("%02x", b & 0xff));
            }
            digest = stringBuffer.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return digest;
    }
}
