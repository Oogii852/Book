package com.library.models;

public class BorrowRecord {
    private int recordId;
    private String memberName;
    private String bookTitle;
    private String borrowDate;
    private String dueDate;
    private String returnDate;
    private String status;

    public BorrowRecord(int recordId, String memberName, String bookTitle,
                        String borrowDate, String dueDate, String returnDate, String status) {
        this.recordId = recordId;
        this.memberName = memberName;
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public int getRecordId() { return recordId; }
    public String getMemberName() { return memberName; }
    public String getBookTitle() { return bookTitle; }
    public String getBorrowDate() { return borrowDate; }
    public String getDueDate() { return dueDate; }
    public String getReturnDate() { return returnDate == null ? "-" : returnDate; }
    public String getStatus() { return status; }
}