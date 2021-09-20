package com.example.schwartzfinalproject;

public class User {

    private String userID;
    private String email;
    private String documentRef;
    private int coins;

    public User() {
    }

    public User(String userID, String email) {
        this.userID = userID;
        this.email = email;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDocumentRef() {
        return documentRef;
    }

    public void setDocumentRef(String documentRef) {
        this.documentRef = documentRef;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", collection='" + documentRef + '\'' +
                '}';
    }
}
