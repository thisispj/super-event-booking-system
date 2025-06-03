package com.demo.supereventbookingsystem.model;

public class User {
    private String username;
    private String password;
    private String preferredName;

    public User(String username, String password, String preferredName) {
        this.username = username;
        this.password = password;
        this.preferredName = preferredName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPreferredName() {
        return preferredName;
    }
}