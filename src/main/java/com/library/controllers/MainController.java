package com.library.controllers;

import com.library.database.DBConnection;
import com.library.models.Book;
import com.library.models.Member;
import com.library.models.Rental;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;

public class MainController {

    @FXML private TextField txtBookName, txtAuthor, txtIsbn, txtQty, txtBookResearch;
    @FXML private Button btnAddBook;
    @FXML private TableView<Book> tableBook;
    @FXML private TableColumn<Book, Integer> colId, colQty, colAv;
    @FXML private TableColumn<Book, String> colTitle, colAuthor, colIsbn;
    private ObservableList<Book> bookList = FXCollections.observableArrayList();
    private int editingBookId = -1;

    @FXML private TextField txtMemberName, txtSurname, txtPhone, txtEmail;
    @FXML private Button btnAddMember;
    @FXML private TableView<Member> tableMember;
    @FXML private TableColumn<Member, Integer> colMId;
    @FXML private TableColumn<Member, String> colMemberName, colMemberSurname, colPhone, colEmail;
    private ObservableList<Member> memberList = FXCollections.observableArrayList();
    private int editingMemberId = -1;

    @FXML private Button btnRentBook, btnReturnBook, btnExtendBook;
    @FXML private RadioButton radioAll, radioOverdue;
    @FXML private TableView<Rental> tableRental;
    @FXML private TableColumn<Rental, Integer> colRentalId;
    @FXML private TableColumn<Rental, String> colRentMemberName, colRentBookTitle,
                                               colRentDate, colReturnDate, colReturnedDate, colRentalStatus;
    private ObservableList<Rental> rentalList = FXCollections.observableArrayList();
    private FilteredList<Rental> filteredRentals;

    @FXML
    public void initialize() {
        if (colId != null) {
            colId.setCellValueFactory(new PropertyValueFactory<>("bookId"));
            colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
            colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
            colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
            colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            colAv.setCellValueFactory(new PropertyValueFactory<>("availableQty"));
            loadBooksFromDatabase();
            FilteredList<Book> filteredBooks = new FilteredList<>(bookList, b -> true);
            if (txtBookResearch != null) {
                txtBookResearch.textProperty().addListener((obs, o, n) -> {
                    filteredBooks.setPredicate(book -> {
                        if (n == null || n.isEmpty()) return true;
                        String lc = n.toLowerCase();
                        return book.getTitle().toLowerCase().contains(lc)
                            || book.getAuthor().toLowerCase().contains(lc)
                            || book.getIsbn().contains(lc);
                    });
                });
            }
            SortedList<Book> sortedBooks = new SortedList<>(filteredBooks);
            sortedBooks.comparatorProperty().bind(tableBook.comparatorProperty());
            tableBook.setItems(sortedBooks);

            // Ном дарахад талбарт утга орно
            tableBook.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    txtBookName.setText(newVal.getTitle());
                    txtAuthor.setText(newVal.getAuthor());
                    txtIsbn.setText(newVal.getIsbn());
                    txtQty.setText(String.valueOf(newVal.getQuantity()));
                    editingBookId = newVal.getBookId();
                    if (btnAddBook != null) btnAddBook.setText("\u0425\u0430\u0434\u0433\u0430\u043B\u0430\u0445");
                } else {
                    txtBookName.clear(); txtAuthor.clear(); txtIsbn.clear(); txtQty.clear();
                    editingBookId = -1;
                    if (btnAddBook != null) btnAddBook.setText("\u041D\u044D\u043C\u044D\u0445");
                }
            });
        }

        if (colMId != null) {
            colMId.setCellValueFactory(new PropertyValueFactory<>("memberId"));
            colMemberName.setCellValueFactory(new PropertyValueFactory<>("memberName"));
            colMemberSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
            colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
            colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
            loadMembers();

            // Уншигч дарахад талбарт утга орно
            tableMember.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    txtMemberName.setText(newVal.getMemberName());
                    txtSurname.setText(newVal.getSurname());
                    txtPhone.setText(newVal.getPhone());
                    txtEmail.setText(newVal.getEmail());
                    editingMemberId = newVal.getMemberId();
                    if (btnAddMember != null) btnAddMember.setText("\u0425\u0430\u0434\u0433\u0430\u043B\u0430\u0445");
                } else {
                    txtMemberName.clear(); txtSurname.clear(); txtPhone.clear(); txtEmail.clear();
                    editingMemberId = -1;
                    if (btnAddMember != null) btnAddMember.setText("\u041D\u044D\u043C\u044D\u0445");
                }
            });
        }

        if (colRentalId != null) {
            colRentalId.setCellValueFactory(new PropertyValueFactory<>("recordId"));
            colRentMemberName.setCellValueFactory(new PropertyValueFactory<>("memberName"));
            colRentBookTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
            colRentDate.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
            colReturnDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
            colReturnedDate.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
            colRentalStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            loadRentalsFromDatabase();
            filteredRentals = new FilteredList<>(rentalList, r -> true);
            tableRental.setItems(filteredRentals);
            ToggleGroup filterGroup = new ToggleGroup();
            radioAll.setToggleGroup(filterGroup);
            radioOverdue.setToggleGroup(filterGroup);
            radioAll.setSelected(true);
            filterGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
                if (newT == radioOverdue) {
                    String today = LocalDate.now().toString();
                    filteredRentals.setPredicate(r ->
                        r.getReturnDate().equals("-") && r.getDueDate().compareTo(today) < 0
                    );
                } else {
                    filteredRentals.setPredicate(r -> true);
                }
            });
        }
    }

    public void loadBooksFromDatabase() {
        bookList.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM book")) {
            while (rs.next()) {
                bookList.add(new Book(rs.getInt("book_id"), rs.getString("title"),
                    rs.getString("author"), rs.getString("isbn"),
                    rs.getInt("quantity"), rs.getInt("available_qty")));
            }
        } catch (SQLException e) { showAlert("\u0410\u043B\u0434\u0430\u0430", e.getMessage()); }
    }

    @FXML
    public void onAddBookClick(ActionEvent event) {
        String title = txtBookName.getText().trim();
        String author = txtAuthor.getText().trim();
        String isbn = txtIsbn.getText().trim();
        String qtyStr = txtQty.getText().trim();
        if (title.isEmpty() || author.isEmpty() || qtyStr.isEmpty()) {
            showAlert("\u0410\u043B\u0434\u0430\u0430", "\u0411\u04AF\u0445 \u0442\u0430\u043B\u0431\u0430\u0440\u044B\u0433 \u0431\u04E9\u0433\u043B\u04E9\u043D\u04E9 \u0443\u0443!"); return;
        }
        int qty;
        try { qty = Integer.parseInt(qtyStr); }
        catch (NumberFormatException e) { showAlert("\u0410\u043B\u0434\u0430\u0430", "\u0422\u043E\u043E \u0448\u0438\u0440\u0445\u044D\u0433 \u0442\u043E\u043E \u0431\u0430\u0439\u0445 \u0451\u0441\u0442\u043E\u0439!"); return; }

        if (editingBookId != -1) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "UPDATE book SET title=?, author=?, isbn=?, quantity=?, available_qty=(? - (SELECT COUNT(*) FROM borrow_records WHERE Bookid=? AND return_date IS NULL)) WHERE book_id=?")) {
                ps.setString(1, title); ps.setString(2, author);
                ps.setString(3, isbn); ps.setInt(4, qty);
                ps.setInt(5, qty); ps.setInt(6, editingBookId); ps.setInt(7, editingBookId);
                ps.executeUpdate();
                loadBooksFromDatabase();
                txtBookName.clear(); txtAuthor.clear(); txtIsbn.clear(); txtQty.clear();
                btnAddBook.setText("\u041D\u044D\u043C\u044D\u0445");
                editingBookId = -1;
                tableBook.getSelectionModel().clearSelection();
                showAlert("\u0410\u043C\u0436\u0438\u043B\u0442\u0442\u0430\u0439", "\u04E8\u04E9\u0440\u0447\u043B\u04E9\u043B\u0442 \u0445\u0430\u0434\u0433\u0430\u043B\u0430\u0433\u0434\u043B\u0430\u0430!");
            } catch (SQLException e) { showAlert("\u0410\u043B\u0434\u0430\u0430", e.getMessage()); }
        } else {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO book (title, author, isbn, quantity, available_qty) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, title); ps.setString(2, author);
                ps.setString(3, isbn); ps.setInt(4, qty); ps.setInt(5, qty);
                ps.executeUpdate();
                loadBooksFromDatabase();
                txtBookName.clear(); txtAuthor.clear(); txtIsbn.clear(); txtQty.clear();
                showAlert("\u0410\u043C\u0436\u0438\u043B\u0442\u0442\u0430\u0439", "\u041D\u043E\u043C \u043D\u044D\u043C\u044D\u0433\u0434\u043B\u044D\u044D!");
            } catch (SQLException e) { showAlert("\u0410\u043B\u0434\u0430\u0430", e.getMessage()); }
        }
    }

    @FXML
    public void onEditBookClick(ActionEvent event) {
        Book selected = tableBook.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("\u0410\u043B\u0434\u0430\u0430", "\u0417\u0430\u0441\u0430\u0445 \u043D\u043E\u043C\u044B\u0433 \u0441\u043E\u043D\u0433\u043E\u043D\u043E \u0443\u0443!"); return; }
        // Click listener аль хэдийн талбарт оруулсан тул зүгээр л focus өгнө
        txtBookName.requestFocus();
    }

    @FXML
    public void onDeleteBookClick(ActionEvent event) {
        Book selected = tableBook.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("\u0410\u043B\u0434\u0430\u0430", "\u0423\u0441\u0442\u0433\u0430\u0445 \u043D\u043E\u043C\u044B\u0433 \u0441\u043E\u043D\u0433\u043E\u043D\u043E \u0443\u0443!"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("\u0423\u0441\u0442\u0433\u0430\u0445");
        confirm.setHeaderText(null);
        confirm.setContentText("\"" + selected.getTitle() + "\" \u043D\u043E\u043C\u044B\u0433 \u0443\u0441\u0442\u0433\u0430\u0445 \u0443\u0443?");
        java.util.Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM book WHERE book_id = ?")) {
                ps.setInt(1, selected.getBookId());
                ps.executeUpdate();
                loadBooksFromDatabase();
                txtBookName.clear(); txtAuthor.clear(); txtIsbn.clear(); txtQty.clear();
                editingBookId = -1;
                showAlert("\u0410\u043C\u0436\u0438\u043B\u0442\u0442\u0430\u0439", "\u041D\u043E\u043C \u0443\u0441\u0442\u0433\u0430\u0433\u0434\u043B\u0430\u0430!");
            } catch (SQLException e) { showAlert("\u0410\u043B\u0434\u0430\u0430", e.getMessage()); }
        }
    }

    private void loadMembers() {
        memberList.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM member")) {
            while (rs.next()) {
                memberList.add(new Member(rs.getInt("member_id"), rs.getString("name"),
                    rs.getString("surname"), rs.getString("phone"), rs.getString("email")));
            }
            tableMember.setItems(memberList);
        } catch (SQLException e) { showAlert("\u0410\u043B\u0434\u0430\u0430", e.getMessage()); }
    }

    @FXML
    public void onAddMemberClick() {
        String name = txtMemberName.getText().trim();
        String surname = txtSurname.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        if (name.isEmpty() || surname.isEmpty()) {
            showAlert("\u0410\u043B\u0434\u0430\u0430", "\u041D\u044D\u0440 \u0431\u043E\u043B\u043E\u043D \u043E\u0432\u0433\u0438\u0439\u0433 \u0431\u04E9\u0433\u043B\u04E9\u043D\u04E9 \u0443\u0443!"); return;
        }
        if (editingMemberId != -1) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "UPDATE member SET name=?, surname=?, phone=?, email=? WHERE member_id=?")) {
                ps.setString(1, name); ps.setString(2, surname);
                ps.setString(3, phone); ps.setString(4, email);
                ps.setInt(5, editingMemberId);
                ps.executeUpdate();
                loadMembers();
                txtMemberName.clear(); txtSurname.clear(); txtPhone.clear(); txtEmail.clear();
                editingMemberId = -1;
                if (btnAddMember != null) btnAddMember.setText("\u041D\u044D\u043C\u044D\u0445");
                tableMember.getSelectionModel().clearSelection();
                showAlert("\u0410\u043C\u0436\u0438\u043B\u0442\u0442\u0430\u0439", "\u04E8\u04E9\u0440\u0447\u043B\u04E9\u043B\u0442 \u0445\u0430\u0434\u0433\u0430\u043B\u0430\u0433\u0434\u043B\u0430\u0430!");
            } catch (SQLException e) { showAlert("\u0410\u043B\u0434\u0430\u0430", e.getMessage()); }
        } else {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO member (surname, name, phone, email) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, surname); ps.setString(2, name);
                ps.setString(3, phone); ps.setString(4, email);
                ps.executeUpdate();
                loadMembers();
                txtMemberName.clear(); txtSurname.clear(); txtPhone.clear(); txtEmail.clear();
                showAlert("\u0410\u043C\u0436\u0438\u043B\u0442\u0442\u0430\u0439", "\u0423\u043D\u0448\u0438\u0433\u0447 \u043D\u044D\u043C\u044D\u0433\u0434\u043B\u044D\u044D!");
            } catch (SQLException e) { showAlert("\u0410\u043B\u0434\u0430\u0430", e.getMessage()); }
        }
    }

    @FXML
    public void onDeleteMemberClick() {
        Member selected = tableMember.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("\u0410\u043B\u0434\u0430\u0430", "\u0423\u0441\u0442\u0433\u0430\u0445 \u0443\u043D\u0448\u0438\u0433\u0447\u0438\u0439\u0433 \u0441\u043E\u043D\u0433\u043E\u043D\u043E \u0443\u0443!"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("\u0423\u0441\u0442\u0433\u0430\u0445");
        confirm.setHeaderText(null);
        confirm.setContentText(selected.getMemberName() + " \u0443\u043D\u0448\u0438\u0433\u0447\u0438\u0439\u0433 \u0443\u0441\u0442\u0433\u0430\u0445 \u0443\u0443?");
        java.util.Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM member WHERE member_id = ?")) {
                ps.setInt(1, selected.getMemberId());
                ps.executeUpdate();
                loadMembers();
                txtMemberName.clear(); txtSurname.clear(); txtPhone.clear(); txtEmail.clear();
                editingMemberId = -1;
                showAlert("\u0410\u043C\u0436\u0438\u043B\u0442\u0442\u0430\u0439", "\u0423\u043D\u0448\u0438\u0433\u0447 \u0443\u0441\u0442\u0433\u0430\u0433\u0434\u043B\u0430\u0430!");
            } catch (SQLException e) { showAlert("\u0410\u043B\u0434\u0430\u0430", e.getMessage()); }
        }
    }

    @FXML
    public void onEditMemberClick() {
        Member selected = tableMember.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("\u0410\u043B\u0434\u0430\u0430", "\u0417\u0430\u0441\u0430\u0445 \u0443\u043D\u0448\u0438\u0433\u0447\u0438\u0439\u0433 \u0441\u043E\u043D\u0433\u043E\u043D\u043E \u0443\u0443!"); return; }
        txtMemberName.requestFocus();
    }

    public void loadRentalsFromDatabase() {
        rentalList.clear();
        String query = "SELECT br.record_id, m.name AS member_name, b.title AS book_title, " +
                       "br.borrow_date, br.due_date, br.return_date, br.status " +
                       "FROM borrow_records br " +
                       "INNER JOIN book b ON br.Bookid = b.book_id " +
                       "INNER JOIN member m ON br.memberid = m.member_id";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                String ret = rs.getString("return_date");
                rentalList.add(new Rental(rs.getInt("record_id"), rs.getString("member_name"),
                    rs.getString("book_title"), rs.getString("borrow_date"),
                    rs.getString("due_date"), ret == null ? "-" : ret, rs.getString("status")));
            }
        } catch (SQLException e) { showAlert("\u0410\u043B\u0434\u0430\u0430", e.getMessage()); }
    }

    @FXML
    public void onRentBookClick(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/library/rent_dialog.fxml"));
            javafx.scene.Parent root = loader.load();
            RentDialogController dialogController = loader.getController();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("\u041D\u043E\u043C\u044B\u043D \u0448\u0438\u043D\u044D \u0442\u04AF\u0440\u044D\u044D\u0441 \u0431\u04AF\u0440\u0442\u0433\u044D\u0445");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            if (dialogController.isSaved()) {
                loadRentalsFromDatabase();
                loadBooksFromDatabase();
            }
        } catch (java.io.IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onExtendBookClick(ActionEvent event) {
        Rental selected = tableRental.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("\u0410\u043B\u0434\u0430\u0430", "\u0421\u0443\u043D\u0433\u0430\u0445 \u0431\u0438\u0447\u043B\u044D\u0433\u0438\u0439\u0433 \u0441\u043E\u043D\u0433\u043E\u043D\u043E \u0443\u0443!"); return;
        }
        if (!selected.getReturnDate().equals("-")) {
            showAlert("\u0410\u043B\u0434\u0430\u0430", "\u042D\u043D\u044D \u043D\u043E\u043C \u0430\u043B\u044C \u0445\u044D\u0434\u0438\u0439\u043D \u0431\u0443\u0446\u0430\u0430\u0433\u0434\u0441\u0430\u043D \u0431\u0430\u0439\u043D\u0430!"); return;
        }
        TextInputDialog dialog = new TextInputDialog("14");
        dialog.setTitle("\u0422\u04AF\u0440\u044D\u044D\u0441 \u0441\u0443\u043D\u0433\u0430\u0445");
        dialog.setHeaderText(null);
        dialog.setContentText("\u04E8\u0434\u0440\u04E9\u04E9\u0440 \u0445\u044D\u0434\u0438\u0439 \u0441\u0443\u043D\u0433\u0430\u0445 \u0432\u0435 (7, 14, 30...):");
        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int days = Integer.parseInt(result.get().trim());
                String newDueDate = LocalDate.parse(selected.getDueDate()).plusDays(days).toString();
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "UPDATE borrow_records SET due_date=?, status='\u0442\u04AF\u0440\u044D\u044D\u0441\u043B\u044D\u0441\u044D\u043D' WHERE record_id=?")) {
                    ps.setString(1, newDueDate);
                    ps.setInt(2, selected.getRecordId());
                    ps.executeUpdate();
                    loadRentalsFromDatabase();
                    showAlert("\u0410\u043C\u0436\u0438\u043B\u0442\u0442\u0430\u0439", "\u0422\u04AF\u0440\u044D\u044D\u0441 " + days + " \u04E9\u0434\u0440\u04E9\u04E9\u0440\u04E9\u04E9\u0440 \u0441\u0443\u043D\u0433\u0430\u0433\u0434\u043B\u0430\u0430!\n\u0428\u0438\u043D\u044D \u0431\u0443\u0446\u0430\u0430\u0445 \u04E9\u0434\u04E9\u0440: " + newDueDate);
                } catch (SQLException e) { showAlert("\u0410\u043B\u0434\u0430\u0430", e.getMessage()); }
            } catch (NumberFormatException e) {
                showAlert("\u0410\u043B\u0434\u0430\u0430", "\u0417\u04E9\u0432\u0445\u04E9\u043D \u0442\u043E\u043E \u0431\u0430\u0439\u0445 \u0451\u0441\u0442\u043E\u0439!");
            }
        }
    }

    @FXML
    public void onReturnBookClick(ActionEvent event) {
        Rental selected = tableRental.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("\u0410\u043B\u0434\u0430\u0430", "\u0411\u0443\u0446\u0430\u0430\u0445 \u0431\u0438\u0447\u043B\u044D\u0433\u0438\u0439\u0433 \u0441\u043E\u043D\u0433\u043E\u043D\u043E \u0443\u0443!"); return;
        }
        if (!selected.getReturnDate().equals("-")) {
            showAlert("\u0410\u043B\u0434\u0430\u0430", "\u042D\u043D\u044D \u043D\u043E\u043C \u0430\u043B\u044C \u0445\u044D\u0434\u0438\u0439\u043D \u0431\u0443\u0446\u0430\u0430\u0433\u0434\u0441\u0430\u043D \u0431\u0430\u0439\u043D\u0430!"); return;
        }
        String newStatus = selected.getDueDate().compareTo(LocalDate.now().toString()) < 0
            ? "\u0445\u0443\u0433\u0430\u0446\u0430\u0430 \u0445\u044D\u0442\u044D\u0440\u0441\u044D\u043D" : "\u0431\u0443\u0446\u0430\u0430\u0441\u0430\u043D";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE borrow_records SET return_date=CURDATE(), status=? WHERE record_id=?")) {
            ps.setString(1, newStatus);
            ps.setInt(2, selected.getRecordId());
            ps.executeUpdate();
            PreparedStatement upd = conn.prepareStatement(
                "UPDATE book b JOIN borrow_records br ON b.book_id=br.Bookid " +
                "SET b.available_qty=b.available_qty+1 WHERE br.record_id=?");
            upd.setInt(1, selected.getRecordId());
            upd.executeUpdate();
            loadRentalsFromDatabase();
            loadBooksFromDatabase();
            showAlert("\u0410\u043C\u0436\u0438\u043B\u0442\u0442\u0430\u0439", "\u041D\u043E\u043C \u0431\u0443\u0446\u0430\u0430\u0433\u0434\u043B\u0430\u0430!");
        } catch (SQLException e) { showAlert("\u0410\u043B\u0434\u0430\u0430", e.getMessage()); }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}