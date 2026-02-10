package com.cryptoproject.spring_shield;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.File;

@Component
public class MainController {

    @Autowired private CryptoService cryptoService;
    @Autowired private DatabaseService dbService;

    @FXML private VBox rootNode;
    @FXML private PasswordField passwordField;
    @FXML private ListView<String> historyList;
    @FXML private VBox rect1, rect2, rect3;
    @FXML private Label strengthLabel;
    @FXML private Label statusLabel; // Ricordati l'fx:id nell'XML!

    private ObservableList<String> historyItems = FXCollections.observableArrayList();
    private Timeline autoLockTimer;

    @FXML
    public void initialize() {
        // Carica la cronologia dal database
        if (dbService != null) historyItems.addAll(dbService.getHistory());
        historyList.setItems(historyItems);

        // Gestione Drag & Drop
        rootNode.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) e.acceptTransferModes(TransferMode.COPY);
            e.consume();
        });
        rootNode.setOnDragDropped(e -> {
            var db = e.getDragboard();
            if (db.hasFiles()) processFile(db.getFiles().get(0), true);
            e.setDropCompleted(true);
            e.consume();
        });

        // Monitoraggio password e reset timer inattività
        passwordField.textProperty().addListener((obs, old, newVal) -> {
            updateStrengthMeter(newVal);
            resetTimer();
        });

        setupAutoLock();
    }

    private void processFile(File file, boolean encrypt) {
        String pw = passwordField.getText();
        if (pw.isEmpty()) {
            updateStatus("ERRORE: INSERISCI PASSWORD!", "#ff4444");
            return;
        }

        try {
            if (encrypt) {
                cryptoService.encryptFile(file, pw);
                addLog("🛡️ SHIELDED: " + file.getName());
                updateStatus("FILE INGHIOTTITO!", "#4caf50");
            } else {
                cryptoService.decryptFile(file, pw);
                addLog("🔓 UNLOCKED: " + file.getName().replace(".shield", ""));
                updateStatus("FILE LIBERATO!", "#4caf50");
            }
            passwordField.clear();
        } catch (Exception e) {
            updateStatus("ERRORE: PASSWORD ERRATA", "#ff4444");
        }
    }

    private void setupAutoLock() {
        autoLockTimer = new Timeline(new KeyFrame(Duration.seconds(60), event -> {
            passwordField.clear();
            updateStatus("AUTO-LOCK ATTIVATO", "#ffca28");
        }));
        autoLockTimer.setCycleCount(1);
        autoLockTimer.play();
    }

    private void resetTimer() {
        if (autoLockTimer != null) autoLockTimer.playFromStart();
    }

    private void updateStatus(String msg, String color) {
        if (statusLabel != null) {
            statusLabel.setText("STATUS: " + msg);
            statusLabel.setStyle("-fx-text-fill: " + color + ";");
        }
    }

    private void addLog(String msg) {
        historyItems.add(0, msg);
        if (dbService != null) dbService.saveToDb(msg, "ACTION");
    }

    private void updateStrengthMeter(String pw) {
        String off = "-fx-background-color: #333333;";
        rect1.setStyle(off); rect2.setStyle(off); rect3.setStyle(off);
        if (pw.length() > 0) rect1.setStyle("-fx-background-color: #ff4444;");
        if (pw.length() > 6) rect2.setStyle("-fx-background-color: #ffca28;");
        if (pw.length() > 10) rect3.setStyle("-fx-background-color: #4caf50;");
    }

    @FXML private void handleEncryptFile() { processFile(pickFile(false), true); }
    @FXML private void handleDecryptFile() { processFile(pickFile(true), false); }
    @FXML private void handleClearHistory() { historyItems.clear(); if (dbService != null) dbService.clearHistory(); }

    private File pickFile(boolean isShield) {
        FileChooser fc = new FileChooser();
        if (isShield) fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Shield", "*.shield"));
        return fc.showOpenDialog(rootNode.getScene().getWindow());
    }

    @FXML
    private void handleOpenFolder() {
        try {
            // Definiamo il percorso della cartella Documenti dell'utente
            String folderPath = System.getProperty("user.home") + File.separator + "Documents";
            File file = new File(folderPath);

            // Se la cartella Documenti non esiste, usiamo la cartella Home principale
            if (!file.exists()) {
                file = new File(System.getProperty("user.home"));
            }

            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Comando per Windows
                new ProcessBuilder("explorer.exe", file.getAbsolutePath()).start();
            } else if (os.contains("mac")) {
                // Comando per macOS
                new ProcessBuilder("open", file.getAbsolutePath()).start();
            } else {
                // Comando per Linux (come nel tuo screenshot)
                new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
            }

            updateStatus("CARTELLA APERTA", "#4caf50");

        } catch (Exception e) {
            e.printStackTrace();
            updateStatus("ERRORE APERTURA CARTELLA", "#ff4444");
        }
    }
}