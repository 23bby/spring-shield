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

    /**
     * Metodo per criptare il file (Il tuo codice originale)
     */
    public void encryptFile(File inputFile, String password) throws Exception {
        byte[] key = generateKey(password);
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] inputBytes = new FileInputStream(inputFile).readAllBytes();
        byte[] outputBytes = cipher.doFinal(inputBytes);

        File outputFile = new File(inputFile.getAbsolutePath() + ".shield");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(outputBytes);
        }

        System.out.println("🚀 File criptato con successo: " + outputFile.getName());
    }

    /**
     * Metodo per decriptare il file
     */
    public void decryptFile(File inputFile, String password) throws Exception {
        // 1. Generiamo la stessa chiave usata per criptare
        byte[] key = generateKey(password);
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        // 2. Prepariamo AES in modalità DECRYPT
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // 3. Leggiamo il file .shield
        byte[] inputBytes = new FileInputStream(inputFile).readAllBytes();
        byte[] outputBytes = cipher.doFinal(inputBytes);

        // 4. Determiniamo il nome del file originale togliendo .shield
        String originalPath = inputFile.getAbsolutePath();
        if (originalPath.endsWith(".shield")) {
            originalPath = originalPath.substring(0, originalPath.length() - 7);
        } else {
            originalPath = originalPath + ".decrypted"; // Sicurezza se il file non ha estensione
        }

        File outputFile = new File(originalPath);
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(outputBytes);
        }

        System.out.println("🔓 File decriptato con successo: " + outputFile.getName());
    }

    /**
     * Metodo di supporto per creare la chiave dalla password (SHA-256)
     * Così evitiamo di ripetere il codice in entrambi i metodi.
     */
    private byte[] generateKey(String password) throws Exception {
        byte[] key = password.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        return Arrays.copyOf(key, 32); // 256 bit
    }
}