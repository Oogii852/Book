package com.library.models;

import javafx.beans.property.*;

public class Rental {
    private final IntegerProperty recordId;
    private final StringProperty memberName;
    private final StringProperty bookTitle;
    private final StringProperty borrowDate;
    private final StringProperty dueDate;
    private final StringProperty returnDate;
    private final StringProperty status;

    public Rental(int recordId, String memberName, String bookTitle,
                  String borrowDate, String dueDate, String returnDate, String status) {
        this.recordId   = new SimpleIntegerProperty(recordId);
        this.memberName = new SimpleStringProperty(memberName);
        this.bookTitle  = new SimpleStringProperty(bookTitle);
        this.borrowDate = new SimpleStringProperty(borrowDate);
        this.dueDate    = new SimpleStringProperty(dueDate);
        this.returnDate = new SimpleStringProperty(returnDate);
        this.status     = new SimpleStringProperty(status);
    }

    public int getRecordId() { return recordId.get(); }
    public IntegerProperty recordIdProperty() { return recordId; }

    public String getMemberName() { return memberName.get(); }
    public StringProperty memberNameProperty() { return memberName; }

    public String getBookTitle() { return bookTitle.get(); }
    public StringProperty bookTitleProperty() { return bookTitle; }

    public String getBorrowDate() { return borrowDate.get(); }
    public StringProperty borrowDateProperty() { return borrowDate; }

    public String getDueDate() { return dueDate.get(); }
    public StringProperty dueDateProperty() { return dueDate; }

    public String getReturnDate() { return returnDate.get(); }
    public StringProperty returnDateProperty() { return returnDate; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
}