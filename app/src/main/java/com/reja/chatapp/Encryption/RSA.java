package com.reja.chatapp.Encryption;

public interface RSA {
    public String getPublicKey();
    public String getPrivateKey();

    public String encryptTextMessage(String text,String publicKey);
    public String decryptTextMessage(String text,String privateKey);
}
