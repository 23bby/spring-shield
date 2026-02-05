package com.cryptoproject.spring_shield;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;

@Service
public class CryptoService {

    public void encryptFile(File inputFile, String password) throws Exception {
        // 1. Trasformiamo la password in una chiave a 256 bit usando SHA-256
        byte[] key = password.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 32); // Assicuriamoci che sia lunga 32 byte (256 bit)

        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        // 2. Prepariamo l'algoritmo AES
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // 3. Leggiamo il file originale
        byte[] inputBytes = new FileInputStream(inputFile).readAllBytes();
        byte[] outputBytes = cipher.doFinal(inputBytes);

        // 4. Salviamo il nuovo file con estensione .shield
        File outputFile = new File(inputFile.getAbsolutePath() + ".shield");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(outputBytes);
        }

        System.out.println("🚀 File criptato con successo: " + outputFile.getName());
    }
}