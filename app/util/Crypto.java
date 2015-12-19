package util;

import play.Logger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    public static String create_sign(Map<String, String> params, String secret) {
        StringBuffer sb = new StringBuffer();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            String value = params.get(key);
            if (key.equals("KEY") || key.equals("URL") || key.equals("sign_data") || key.equals("sign_type")) {
                continue;
            }
            if(sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s", key, value));
        }

        String pre_sign = sb.toString();
        Logger.debug(pre_sign);

        return md5(pre_sign + secret);

    }
}
