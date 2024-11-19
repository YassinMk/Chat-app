package com.omar.chatapp.model;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private int userId;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User(String username, int userId, String password) {
        this.username = username;
        this.userId = userId;
        this.password = password;
    }

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                ", userId='" + userId + '\'' +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
