package com.cryptoproject.spring_shield;

import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseService {

    private static final String URL = "jdbc:sqlite:crocco_shield.db";

    public DatabaseService() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            // Creiamo la tabella se non esiste
            stmt.execute("CREATE TABLE IF NOT EXISTS history (id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT, status TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveToDb(String filePath, String status) {
        String sql = "INSERT INTO history(path, status) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filePath);
            pstmt.setString(2, status);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getHistory() {
        List<String> history = new ArrayList<>();
        String sql = "SELECT path, status FROM history ORDER BY id DESC LIMIT 50";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String prefix = rs.getString("status").equals("SHIELDED") ? "🛡️ SHIELDED: " : "🔓 UNLOCKED: ";
                history.add(prefix + rs.getString("path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public void clearHistory() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM history");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}