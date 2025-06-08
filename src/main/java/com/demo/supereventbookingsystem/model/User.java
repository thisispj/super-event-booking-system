package com.demo.supereventbookingsystem.model;

import java.sql.Timestamp;

public class User {
    private String username;
    private String password;
    private String preferredName;
    private Timestamp memberSince;
    private int userTypeId;

    // Constructor for database loading (matches getAllUsers/getUserByUsername)
    public User(String username, String preferredName, Timestamp memberSince, int userTypeId) {
        this.username = username;
        this.preferredName = preferredName;
        this.memberSince = memberSince;
        this.userTypeId = userTypeId;
    }

    // Constructor with password (for user creation or login)
    public User(String username, String password, String preferredName, int userTypeId) {
        this.username = username;
        this.password = password;
        this.preferredName = preferredName;
        this.userTypeId = userTypeId;
        this.memberSince = new Timestamp(System.currentTimeMillis()); // Default to current time
    }

    // Constructor with default user type (for convenience)
    public User(String username, String password, String preferredName) {
        this(username, password, preferredName, 1); // Default to regular user
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public Timestamp getMemberSince() {
        return memberSince;
    }

    public int getUserTypeId() {
        return userTypeId;
    }

    // Setters (if needed, add as required)
    public void setPassword(String password) {
        this.password = password;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public void setMemberSince(Timestamp memberSince) {
        this.memberSince = memberSince;
    }

    public void setUserTypeId(int userTypeId) {
        this.userTypeId = userTypeId;
    }
}