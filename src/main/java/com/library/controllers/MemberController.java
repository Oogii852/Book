package com.library.controllers;

import com.library.database.DBConnection;
import com.library.models.Member;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;

public class MemberController {

    @FXML private TextField txtMemberName;
    @FXML private TextField txtSurname;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;

    @FXML private TableView<Member> tableMember;
    @FXML private TableColumn<Member, Integer> colMemberId;
    @FXML private TableColumn<Member, String> colMemberName;
    @FXML private TableColumn<Member, String> colSurname;
    @FXML private TableColumn<Member, String> colPhone;
    @FXML private TableColumn<Member, String> colEmail;

    private ObservableList<Member> memberList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colMemberId.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        colMemberName.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        loadMembers();
    }

    private void loadMembers() {
        memberList.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM member")) {
            while (rs.next()) {
                memberList.add(new Member(
                    rs.getInt("member_id"),
                    rs.getString("name"),
                    rs.getString("surname"),
                    rs.getString("phone"),
                    rs.getString("email")
                ));
            }
            tableMember.setItems(memberList);
        } catch (SQLException e) {
            showAlert("Алдаа", e.getMessage());
        }
    }

    @FXML
    public void onAddMemberClick() {
        String name = txtMemberName.getText().trim();
        String surname = txtSurname.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty() || surname.isEmpty()) {
            showAlert("Алдаа", "Нэр болон овгийг бөглөнө үү!");
            return;
        }

        String sql = "INSERT INTO member (surname, name, phone, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, surname);
            ps.setString(2, name);
            ps.setString(3, phone);
            ps.setString(4, email);
            ps.executeUpdate();
            loadMembers();
            txtMemberName.clear();
            txtSurname.clear();
            txtPhone.clear();
            txtEmail.clear();
            showAlert("Амжилттай", "Үншигч нэмэгдлээ!");
        } catch (SQLException e) {
            showAlert("Алдаа", e.getMessage());
        }
    }

    @FXML
    public void onDeleteMemberClick() {
        Member selected = tableMember.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Алдаа", "Устгах гишүүнийг сонгоно уу!");
            return;
        }
        String sql = "DELETE FROM member WHERE member_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, selected.getMemberId());
            ps.executeUpdate();
            loadMembers();
            showAlert("Амжилттай", "Үншигч устгагдлаа!");
        } catch (SQLException e) {
            showAlert("Алдаа", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}