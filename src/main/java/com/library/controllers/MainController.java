package com.library.controllers;

import com.library.database.DBConnection;
import com.library.models.Book;
import com.library.models.Member;
import com.library.models.Rental;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;

public class MainController {

    @FXML private TableView<Book> tableBook;
    @FXML private TableColumn<Book, Integer> colId, colQty, colAv;
    @FXML private TableColumn<Book, String> colTitle, colAuthor, colIsbn;
    @FXML private TextField txtAuthor, txtBookName, txtBookResearch, txtIsbn, txtQty;
    private ObservableList<Book> bookList = FXCollections.observableArrayList();

    @FXML private TableView<Member> tableMember;
    @FXML private TableColumn<Member, Integer> colMId;
    @FXML private TableColumn<Member, String> colMemberName, colMemberSurname, colPhone, colEmail;
    @FXML private TextField txtMemberName, txtSurname, txtPhone, txtEmail, txtMemberSearch;
    private ObservableList<Member> memberList = FXCollections.observableArrayList();

    @FXML private Button btnRentBook, btnReturnBook;
    @FXML private RadioButton radioAll, radioOverdue;
    @FXML private ToggleGroup filterGroup;
    @FXML private TableView<Rental> tableRental;
    @FXML private TableColumn<Rental, Integer> colRentalId;
    @FXML private TableColumn<Rental, String> colRentMemberName, colRentBookTitle;
    @FXML private TableColumn<Rental, String> colRentDate, colReturnDate, colRentalStatus;
    private ObservableList<Rental> rentalList = FXCollections.observableArrayList();
    private FilteredList<Rental> filteredRentals;

    @FXML
    public void initialize() {
        // Номын баганууд
        colId.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colAv.setCellValueFactory(new PropertyValueFactory<>("availableQty"));

        // Уншигчийн баганууд
        colMId.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        colMemberName.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        colMemberSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Түрээсийн баганууд
        colRentalId.setCellValueFactory(new PropertyValueFactory<>("recordId"));
        colRentMemberName.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        colRentBookTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colRentDate.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        colReturnDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colRentalStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadBooksFromDatabase();
        loadMembersFromDatabase();
        loadRentalsFromDatabase();

        // Номын хайлт
        FilteredList<Book> filteredBooks = new FilteredList<>(bookList, b -> true);
        txtBookResearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredBooks.setPredicate(book -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String f = newVal.toLowerCase();
                return book.getTitle().toLowerCase().contains(f)
                    || book.getAuthor().toLowerCase().contains(f)
                    || book.getIsbn().toLowerCase().contains(f);
            });
        });
        tableBook.setItems(filteredBooks);

        // Уншигчийн хайлт
        FilteredList<Member> filteredMembers = new FilteredList<>(memberList, m -> true);
        txtMemberSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredMembers.setPredicate(member -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String f = newVal.toLowerCase();
                return member.getMemberName().toLowerCase().contains(f)
                    || member.getSurname().toLowerCase().contains(f)
                    || member.getPhone().toLowerCase().contains(f);
            });
        });
        tableMember.setItems(filteredMembers);

        // Түрээсийн шүүлтүүр
        filteredRentals = new FilteredList<>(rentalList, r -> true);
        tableRental.setItems(filteredRentals);

        if (filterGroup != null) {
            filterGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
                if (newT == radioOverdue) {
                    filteredRentals.setPredicate(r -> r.getStatus().equalsIgnoreCase("хэтэрсэн"));
                } else {
                    filteredRentals.setPredicate(r -> true);
                }
            });
        }
    }

    private void loadBooksFromDatabase() {
        bookList.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM book")) {
            while (rs.next()) {
                bookList.add(new Book(
                    rs.getInt("book_id"), rs.getString("title"),
                    rs.getString("author"), rs.getString("isbn"),
                    rs.getInt("quantity"), rs.getInt("available_qty")
                ));
            }
        } catch (SQLException e) { showAlert("Алдаа", e.getMessage()); }
    }

    @FXML
    public void onAddBookClick(ActionEvent event) {
        String title = txtBookName.getText();
        String author = txtAuthor.getText();
        String isbn = txtIsbn.getText();
        String qty = txtQty.getText();
        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || qty.isEmpty()) {
            showAlert("Алдаа", "Бүх талбарыг бөглөнө үү!"); return;
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO book (title, author, isbn, quantity, available_qty) VALUES (?,?,?,?,?)")) {
            ps.setString(1, title); ps.setString(2, author); ps.setString(3, isbn);
            ps.setInt(4, Integer.parseInt(qty)); ps.setInt(5, Integer.parseInt(qty));
            ps.executeUpdate();
            loadBooksFromDatabase();
            txtBookName.clear(); txtAuthor.clear(); txtIsbn.clear(); txtQty.clear();
            showAlert("Амжилттай", "Ном нэмэгдлээ!");
        } catch (SQLException e) { showAlert("Алдаа", e.getMessage()); }
    }

    private void loadMembersFromDatabase() {
        memberList.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM member")) {
            while (rs.next()) {
                memberList.add(new Member(
                    rs.getInt("member_id"), rs.getString("name"),
                    rs.getString("surname"), rs.getString("phone"), rs.getString("email")
                ));
            }
        } catch (SQLException e) { showAlert("Алдаа", e.getMessage()); }
    }

    @FXML
    public void onAddMemberClick(ActionEvent event) {
        String name = txtMemberName.getText();
        String surname = txtSurname.getText();
        String phone = txtPhone.getText();
        String email = txtEmail.getText();
        if (name.isEmpty() || surname.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showAlert("Алдаа", "Бүх талбарыг бөглөнө үү!"); return;
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO member (name, surname, phone, email) VALUES (?,?,?,?)")) {
            ps.setString(1, name); ps.setString(2, surname);
            ps.setString(3, phone); ps.setString(4, email);
            ps.executeUpdate();
            loadMembersFromDatabase();
            txtMemberName.clear(); txtSurname.clear(); txtPhone.clear(); txtEmail.clear();
            showAlert("Амжилттай", "Уншигч нэмэгдлээ!");
        } catch (SQLException e) { showAlert("Алдаа", e.getMessage()); }
    }

    private void loadRentalsFromDatabase() {
        rentalList.clear();
        String query = "SELECT br.record_id, m.name AS member_name, b.title AS book_title, " +
                       "br.borrow_date, br.due_date, br.return_date, br.status " +
                       "FROM borrow_record br " +
                       "INNER JOIN book b ON br.bookid = b.book_id " +
                       "INNER JOIN member m ON br.memberid = m.member_id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String ret = rs.getString("return_date");
                if (ret == null || ret.isEmpty()) ret = "-";
                rentalList.add(new Rental(
                    rs.getInt("record_id"),
                    rs.getString("member_name"),
                    rs.getString("book_title"),
                    rs.getString("borrow_date"),
                    rs.getString("due_date"),
                    ret,
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) { showAlert("Алдаа", e.getMessage()); }
    }

    @FXML
    void onRentBookClick(ActionEvent event) {
        showAlert("Түрээс", "Ном түрээслэх товч дарагдлаа!");
    }

    @FXML
    void onReturnBookClick(ActionEvent event) {
        showAlert("Буцаах", "Ном буцаах товч дарагдлаа!");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}