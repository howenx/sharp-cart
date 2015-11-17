package controllers;

import org.apache.xerces.impl.dv.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA加密
 * Created by howen on 15/11/16.
 */
public class Encryption {

    private String privateKey;
    private String publicKey;

    public static Encryption getInstance() {

        return EncryptionHolder.INSTANCE;
    }

    private static class EncryptionHolder {
        private static final Encryption INSTANCE = new Encryption();
    }

    /**
     * 随机生成密钥对
     */
    public static void genKeyPair(String filePath) {

        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
            // 初始化密钥对生成器，密钥大小为512位
            keyPairGen.initialize(512, new SecureRandom());
            // 生成一个密钥对，保存在keyPair中
            KeyPair keyPair = keyPairGen.generateKeyPair();
            // 得到私钥
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            // 得到公钥
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            // 得到公钥字符串
            String publicKeyString = Base64.encode(publicKey.getEncoded());
            // 得到私钥字符串
            String privateKeyString = Base64.encode(privateKey.getEncoded());
            // 将密钥对写入到文件
            FileWriter pubfw = new FileWriter(filePath + "/publicKey.keystore");
            FileWriter prifw = new FileWriter(filePath + "/privateKey.keystore");
            BufferedWriter pubbw = new BufferedWriter(pubfw);
            BufferedWriter pribw = new BufferedWriter(prifw);
            pubbw.write(publicKeyString);
            pribw.write(privateKeyString);
            pubbw.flush();
            pubbw.close();
            pubfw.close();
            pribw.flush();
            pribw.close();
            prifw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Encryption() {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
            // 初始化密钥对生成器，密钥大小为512位
            keyPairGen.initialize(512, new SecureRandom());
            // 生成一个密钥对，保存在keyPair中
            KeyPair keyPair = keyPairGen.generateKeyPair();
            // 得到私钥
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
            // 得到公钥
            RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();

            // 得到公钥字符串
            String publicKeyString = Base64.encode(rsaPrivateKey.getEncoded());
            // 得到私钥字符串
            String privateKeyString = Base64.encode(rsaPublicKey.getEncoded());

            this.privateKey = publicKeyString;
            this.publicKey = privateKeyString;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 私钥加密
     * @param privatekey  私钥字符串
     * @param content   明文
     * @return 加密或后的base64字符串
     */
    public static String handleEncrypt(String privatekey, String content) {
        byte[] output = null;

        try {
            // 使用默认RSA
            Cipher cipher = cipher = Cipher.getInstance("RSA");

            cipher.init(Cipher.ENCRYPT_MODE, loadPrivateKeyByStr(privatekey));

            output= cipher.doFinal(content.getBytes("UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Base64.encode(output);
    }

    /**
     * 公钥解密
     * @param publickey 公钥
     * @param content   加密的Base64字符串
     * @return 明文
     */
    public static String handleDecrypt(String publickey, String content) throws UnsupportedEncodingException {
        byte[] output = null;

        try {
            // 使用默认RSA
            Cipher cipher = cipher = Cipher.getInstance("RSA");

            cipher.init(Cipher.DECRYPT_MODE, loadPublicKeyByStr(publickey));

            output= cipher.doFinal(Base64.decode(content));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(output,"UTF-8");
    }

    /**
     * 从字符串中加载公钥
     *
     * @param publicKeyStr
     *            公钥数据字符串
     * @throws Exception
     *             加载公钥时产生的异常
     */
    public static RSAPublicKey loadPublicKeyByStr(String publicKeyStr)
            throws Exception {
        try {
            byte[] buffer = Base64.decode(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }

    /**
     * 从字符串中加载私钥
     *
     * @param privateKeyStr
     *            公钥数据字符串
     * @throws Exception
     *             加载公钥时产生的异常
     */
    public static RSAPrivateKey loadPrivateKeyByStr(String privateKeyStr)
            throws Exception {
        try {
            byte[] buffer = Base64.decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("私钥非法");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }

    protected String getPrivateKey() {
        return privateKey;
    }

    private void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    private void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
