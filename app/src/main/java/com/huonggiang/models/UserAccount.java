package com.huonggiang.models;

import java.io.Serializable;

public class UserAccount implements Serializable {
    private String userName;
    private String password;
    private String role;
    private String displayName;
    private boolean saved;

    public UserAccount(String userName, String password, String role, String displayName, String saved) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.displayName = displayName;
        this.saved = saved != null && saved.equalsIgnoreCase("saved");
    }

    public UserAccount(String userName, String password, String role, String displayName) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.displayName = displayName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", displayName='" + displayName + '\'' +
                ", saved=" + saved +
                '}';
    }
}