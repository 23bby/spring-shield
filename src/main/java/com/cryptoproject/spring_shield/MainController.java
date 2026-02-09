package com.cryptoproject.spring_shield;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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

    private ObservableList<String> encryptedFiles = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        encryptedFiles.addAll(dbService.getHistory());
        if (historyList != null) historyList.setItems(encryptedFiles);

        rootNode.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        });

        rootNode.setOnDragDropped(event -> {
            var db = event.getDragboard();
            if (db.hasFiles()) handleQuickShield(db.getFiles().get(0));
            event.setDropCompleted(true);
            event.consume();
        });

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> updateStrengthMeter(newVal));
    }

    private void updateStrengthMeter(String pw) {
        String off = "-fx-background-color: #333333;";
        rect1.setStyle(off); rect2.setStyle(off); rect3.setStyle(off);
        if (pw.isEmpty()) { strengthLabel.setText("WAITING FOR KEY..."); }
        else if (pw.length() < 6) { rect1.setStyle("-fx-background-color: #ff4444;"); strengthLabel.setText("WEAK"); }
        else if (pw.length() < 10) { rect1.setStyle("-fx-background-color: #ffca28;"); rect2.setStyle("-fx-background-color: #ffca28;"); strengthLabel.setText("MEDIUM"); }
        else { rect1.setStyle("-fx-background-color: #4caf50;"); rect2.setStyle("-fx-background-color: #4caf50;"); rect3.setStyle("-fx-background-color: #4caf50;"); strengthLabel.setText("STRONG"); }
    }

    @FXML
    private void handleEncryptFile() { processFile(pickFile("Select file", false), true); }

    @FXML
    private void handleDecryptFile() { processFile(pickFile("Select .shield file", true), false); }

    private void handleQuickShield(File file) { processFile(file, true); }

    private void processFile(File file, boolean encrypt) {
        String password = passwordField.getText();
        if (password.isEmpty()) { showNotify("Attention", "Enter password!", Alert.AlertType.WARNING); return; }
        if (file != null) {
            try {
                if (encrypt) {
                    cryptoService.encryptFile(file, password);
                    String newPath = file.getAbsolutePath() + ".shield";
                    dbService.saveToDb(newPath, "SHIELDED");
                    encryptedFiles.add(0, "🛡️ SHIELDED: " + newPath);
                } else {
                    cryptoService.decryptFile(file, password);
                    String newPath = file.getAbsolutePath().replace(".shield", "");
                    dbService.saveToDb(newPath, "UNSHIELDED");
                    encryptedFiles.add(0, "🔓 UNLOCKED: " + newPath);
                }
                passwordField.clear();
            } catch (Exception e) { showNotify("Error", "Failed!", Alert.AlertType.ERROR); }
        }
    }

    @FXML
    private void handleOpenFolder() {
        String selected = historyList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Estraiamo il percorso saltando il prefisso "🛡️ SHIELDED: "
        String fullPath = selected.substring(selected.indexOf(": ") + 2);
        File file = new File(fullPath);
        File parentDir = file.getParentFile(); // Questa è la cartella che contiene il file

        if (parentDir != null && parentDir.exists()) {
            try {
                ProcessBuilder pb;
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    pb = new ProcessBuilder("explorer.exe", parentDir.getAbsolutePath());
                } else {
                    pb = new ProcessBuilder("xdg-open", parentDir.getAbsolutePath());
                }
                pb.start();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void handleClearHistory() { dbService.clearHistory(); encryptedFiles.clear(); }

    private File pickFile(String t, boolean s) {
        FileChooser fc = new FileChooser(); fc.setTitle(t);
        if (s) fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Shield Files", "*.shield"));
        return fc.showOpenDialog(new Stage());
    }

    private void showNotify(String t, String m, Alert.AlertType type) {
        Alert a = new Alert(type); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}