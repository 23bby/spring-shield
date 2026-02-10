package com.cryptoproject.spring_shield;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;

@Service // FONDAMENTALE: permette a Spring di trovare questa classe
public class CryptoService {

    private static final String ALGORITHM = "AES";
    private static final String MAGIC_WORD = "CROCCO";

    public void encryptFile(File inputFile, String password) throws Exception {
        byte[] key = generateKey(password);
        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        File outputFile = new File(inputFile.getAbsolutePath() + ".shield");

        // Leggiamo tutto il file, aggiungiamo la firma e criptiamo
        byte[] inputBytes = Files.readAllBytes(inputFile.toPath());
        byte[] encryptedBytes = cipher.doFinal(inputBytes);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(MAGIC_WORD.getBytes()); // Firma identificativa
            fos.write(encryptedBytes);
        }

        // Se la protezione è riuscita, eliminiamo l'originale
        if (outputFile.exists()) {
            Files.delete(inputFile.toPath());
        }
    }

    public void decryptFile(File inputFile, String password) throws Exception {
        byte[] key = generateKey(password);
        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        try (FileInputStream fis = new FileInputStream(inputFile)) {
            // Verifica firma "CROCCO"
            byte[] header = new byte[MAGIC_WORD.length()];
            fis.read(header);
            if (!new String(header).equals(MAGIC_WORD)) {
                throw new Exception("Firma non valida! Questo non è un file del Crocco.");
            }

            byte[] encryptedBytes = fis.readAllBytes();
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            File outputFile = new File(inputFile.getAbsolutePath().replace(".shield", ""));
            Files.write(outputFile.toPath(), decryptedBytes);

            // Se liberato con successo, eliminiamo il file .shield
            Files.delete(inputFile.toPath());
        }
    }

    private byte[] generateKey(String password) {
        byte[] key = new byte[16];
        byte[] passBytes = password.getBytes();
        for (int i = 0; i < 16; i++) {
            key[i] = (i < passBytes.length) ? passBytes[i] : 0;
        }
        return key;
    }
}