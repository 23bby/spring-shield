package com.cryptoproject.spring_shield;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.File;

@Component
public class MainController {

    @Autowired
    private CryptoService cryptoService; // Spring inietterà automaticamente il servizio qui
    // Questo "id" deve essere identico a quello nel file .fxml
    @FXML
    private PasswordField passwordField;

    @FXML
    public void initialize() {
        System.out.println("✅ Controller inizializzato e pronto!");
    }

    @FXML
    private void handleSelectFile() {
        // 1. Prima controlliamo la password
        String password = passwordField.getText();

        if (password.isEmpty()) {
            System.out.println("❌ ERRORE: Devi inserire una password prima di scegliere il file!");
            // Qui potremmo aggiungere un avviso grafico in futuro
            return;
        }

        // 2. Se la password c'è, apriamo il selettore file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona il file da criptare");

        // Filtro opzionale: mostra solo certi file (es. .txt, .pdf)
        // fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documenti", "*.txt", "*.pdf"));

        File selectedFile = fileChooser.showOpenDialog(new Stage());

        // 3. Risultato finale
        if (selectedFile != null) {
            try {
                // Qui chiamiamo il tuo CryptoService!
                cryptoService.encryptFile(selectedFile, password);
                System.out.println("✅ CRIPTAZIONE COMPLETATA!");
            } catch (Exception e) {
                System.out.println("❌ Errore critico: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}