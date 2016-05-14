package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import play.Application;
import play.Logger;
import play.libs.Json;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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
            Logger.error(Throwables.getStackTraceAsString(e));
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


    /**
     * 签名生成算法
     * @param params 请求参数集，所有参数必须已转换为字符串类型
     * @param secret 签名密钥
     * @return 签名
     * @throws IOException
     */
    public static String getSignature(Map<String,String> params, String secret) throws IOException
    {
        // 先将参数以其参数名的字典序升序进行排序
        Map<String, String> sortedParams = new TreeMap<>(params);
        Set<Map.Entry<String, String>> entrys = sortedParams.entrySet();

        // 遍历排序后的字典，将所有参数按"key=value"格式拼接在一起
        StringBuilder basestring = new StringBuilder();
        for (Map.Entry<String, String> param : entrys) {
            basestring.append(param.getKey()).append("=").append(param.getValue());
        }
        basestring.append(secret);

        // 使用MD5对待签名串求签
        byte[] bytes = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            bytes = md5.digest(basestring.toString().getBytes("UTF-8"));
        } catch (GeneralSecurityException ex) {
            throw new IOException(ex);
        }

        // 将MD5输出的二进制结果转换为小写的十六进制
        StringBuilder sign = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex);
        }
        return sign.toString();
    }
}
