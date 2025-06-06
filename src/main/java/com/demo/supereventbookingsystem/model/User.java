package com.demo.supereventbookingsystem.model;

public class User {
    private String username;
    private String password;
    private String preferredName;
    private int userTypeId;

    public User(String username, String password, String preferredName, int userTypeId) {
        this.username = username;
        this.password = password;
        this.preferredName = preferredName;
        this.userTypeId = userTypeId;
    }

    public User(String username, String password, String preferredName) {
        this(username, password, preferredName, 1); // Default to regular user
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPreferredName() { return preferredName; }
    public int getUserTypeId() { return userTypeId; }
    public void setUserTypeId(int userTypeId) { this.userTypeId = userTypeId; }
}