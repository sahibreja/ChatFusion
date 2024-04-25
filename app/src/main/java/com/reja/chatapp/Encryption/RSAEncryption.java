package com.reja.chatapp.Encryption;

import android.os.Build;
import androidx.annotation.RequiresApi;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

public class RSAEncryption implements RSA {
    private static final String RSA_ALGORITHM = "RSA";

    // Generate RSA key pair
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    // Convert PublicKey to String
    private String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    // Convert PrivateKey to String
    private String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    // Convert String to PublicKey
    private PublicKey stringToPublicKey(String publicKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = new byte[0];
        publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
        // Assuming you are using RSA_ALGORITHM
        // You can also use KeyFactory.getInstance("RSA") instead of directly using RSA algorithm
        return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    }

    // Convert String to PrivateKey
    private PrivateKey stringToPrivateKey(String privateKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKeyBytes = new byte[0];
        privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
        // Assuming you are using RSA_ALGORITHM
        // You can also use KeyFactory.getInstance("RSA") instead of directly using RSA algorithm
        return KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
    }

    // Encrypt text using RSA public key

    private String encryptText(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decrypt text using RSA private key
    private String decryptText(String encryptedText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes);
    }

    @Override
    public String getPublicKey() {
        try {
            KeyPair keyPair = generateKeyPair();
            return publicKeyToString(keyPair.getPublic());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getPrivateKey() {
        try {
            KeyPair keyPair = generateKeyPair();
            return privateKeyToString(keyPair.getPrivate());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String encryptTextMessage(String text, String publicKey) {
        try {
            PublicKey rsaPublicKey = stringToPublicKey(publicKey);
            return encryptText(text, rsaPublicKey);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String decryptTextMessage(String text, String privateKey) {
        try {
            PrivateKey rsaPrivateKey = stringToPrivateKey(privateKey);
            return decryptText(text, rsaPrivateKey);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
