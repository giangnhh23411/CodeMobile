package com.huonggiang.models;

import java.util.ArrayList;

public class ListUserAccount {
    public static ArrayList<UserAccount> getUserAccount() {
        ArrayList<UserAccount> database = new ArrayList<>();
        database.add(new UserAccount("admin", "123", "Administrator", "Nguyen Hoang Huong Giang", "saved"));
        database.add(new UserAccount("u1", "123", "Employee", "Nguyen Van A", "saved"));
        database.add(new UserAccount("u2", "123", "Employee", "Le Thi B", "saved"));
        return database;
    }

    public static UserAccount login(String username, String password) {
        ArrayList<UserAccount> database = getUserAccount();
        for (UserAccount acc : database) {
            if (acc.getUserName().equalsIgnoreCase(username) && acc.getPassword().equals(password)) {
                return acc;
            }
        }
        return null;
    }
}
