package com.midas.consulting.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final byte[] keyValue = "SecretKeyToGenJWTs".getBytes();//new byte[]{'T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'};

    public static String encrypt(String valueToEnc) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(128);
        SecretKey secretKey = new SecretKeySpec(keyValue, ALGORITHM);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedValue = cipher.doFinal(valueToEnc.getBytes());
        return Base64.getEncoder().encodeToString(encryptedValue);
    }

    public static String decrypt(String encryptedValue) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(128);
        SecretKey secretKey = new SecretKeySpec(keyValue, ALGORITHM);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decryptedValue = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
        return new String(decryptedValue);
    }

    public static void main(String[] args) {
        try {
          String plain=  decrypt("$2a$10$C9GFB0GpG4FYMnHab0q0U.4rn1GGyf4P3Rnvh2Jb8Wy7NfkfXa8.u");
          System.out.println(plain);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
