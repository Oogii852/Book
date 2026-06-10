package com.library.controllers;

import java.time.LocalDate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.library.database.DBConnection;
import java.sql.*;

public class RentDialogController {

    @FXML private ComboBox<String> cmbBook;
    @FXML private ComboBox<String> cmbMember;
    @FXML private DatePicker dpDueDate;

    // Түрээсийн мэдээллийг хадгалах төлөв
    private boolean isSaved = false;

    @FXML
    public void initialize() {
        loadMembers();
        loadAvailableBooks();
        // Анхдагчаар 14 хоногийн хугацаа өгнө
        dpDueDate.setValue(LocalDate.now().plusDays(14));
    }

    public boolean isSaved() {
        return isSaved;
    }

    private void loadMembers() {
        String query = "SELECT member_id, name FROM member";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // ID ба уншигчийн нэрийг хөлбоо харуулна (Жишээ нь: "1 - Болд")
                cmbMember.getItems().add(rs.getInt("member_id") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAvailableBooks() {
        String query = "SELECT book_id, title, available_qty FROM book WHERE available_qty > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // ID, нэр, үлдэгдэл тоо харуулна (Жишээ нь: "1 - Жаран цагаан хонь /10/")
                cmbBook.getItems().add(rs.getInt("book_id") + " - " + rs.getString("title") + " /" + rs.getInt("available_qty") + "/");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onCancelClick(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) cmbMember.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void onSaveClick(ActionEvent event) {
        if (cmbMember.getValue() == null || cmbBook.getValue() == null || dpDueDate.getValue() == null) {
            showAlert("Алдаа", "Бүх талбарыг бүрэн бөглөнө үү!", Alert.AlertType.ERROR);
            return;
        }

        // ComboBox-оос ID-г салгаж авах (Жишээ: "1 - Болд" -> 1)
        int memberId = Integer.parseInt(cmbMember.getValue().split(" - ")[0]);
        int bookId   = Integer.parseInt(cmbBook.getValue().split(" - ")[0]);
        String borrowDate = LocalDate.now().toString();
        String dueDate    = dpDueDate.getValue().toString();

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            // Номын түрээс буртгэх ӨС хүснэгт руу хадгалах өгөгдлүүдийг бэлтгэх
            conn.setAutoCommit(false);

            // Түрээсийн гүйцэд намаж (borrow_records)
            String insertQuery = "INSERT INTO borrow_records (borrow_date, due_date, status, bookid, memberid) VALUES (?, ?, 'түрээслэсэн', ?, ?)";
            try (PreparedStatement stmtInsert = conn.prepareStatement(insertQuery)) {
                stmtInsert.setString(1, borrowDate);
                stmtInsert.setString(2, dueDate);
                stmtInsert.setInt(3, bookId);
                stmtInsert.setInt(4, memberId);
                stmtInsert.executeUpdate();
            }

            // Номын бэлэн үлдэгдлийг (available_qty) 1-ээр хасах
            String updateQuery = "UPDATE book SET available_qty = available_qty - 1 WHERE book_id = ?";
            try (PreparedStatement stmtUpdate = conn.prepareStatement(updateQuery)) {
                stmtUpdate.setInt(1, bookId);
                stmtUpdate.executeUpdate();
            }

            conn.commit();
            isSaved = true;
            closeStage();

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            showAlert("Алдаа", e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}