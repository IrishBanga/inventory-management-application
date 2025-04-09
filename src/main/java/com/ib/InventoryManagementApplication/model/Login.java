package com.ib.InventoryManagementApplication.model;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Login {
    private static final List<Pair<String, String>> loginCredentials = new ArrayList<>(List.of(
            new Pair<>("admin", "admin"), new Pair<>("ib", "ib")
    ));
    private final String userName;
    private final String password;

    public Login(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public boolean validate() {
        Pair<String, String> loginCred = new Pair<>(this.userName, this.password);
        return loginCredentials.contains(loginCred);
    }

}
