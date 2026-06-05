package com.library.models;

public class Member {
    private int memberId;
    private String memberName;
    private String surname;
    private String phone;
    private String email;

    public Member() {}

    public Member(int memberId, String memberName, String surname, String phone, String email) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
    }

    public int getId() { return memberId; }
    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}