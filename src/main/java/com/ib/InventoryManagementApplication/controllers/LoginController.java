package com.ib.InventoryManagementApplication.controllers;

import com.ib.InventoryManagementApplication.model.Login;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;

import static com.ib.InventoryManagementApplication.utility.Helper.showAlert;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button cancelButtonLoginTab;
    @FXML
    private Tab loginTab;
    @FXML
    private Tab inventoryTab;
    @FXML
    private TabPane tabPane;

    @FXML
    public void initialize() {
        // register event handlers
        loginButton.setOnAction(event -> handleLogin());
        cancelButtonLoginTab.setOnAction(event -> {
            ((Stage) cancelButtonLoginTab.getScene().getWindow()).close();
        });
    }

    public void handleLogin() {
        if (!usernameField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            Login loginCred = new Login(usernameField.getText(), passwordField.getText());
            // validate the login credentials
            if (loginCred.validate()) {
                // disable the login tab and enable the profile tab
                loginTab.setDisable(true);
                inventoryTab.setDisable(false);
                // load the profile tab and set the header
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ib/InventoryManagementApplication/inventory-tab.fxml"));
                try {
                    tabPane.getSelectionModel().select(inventoryTab);
                    inventoryTab.setContent(fxmlLoader.load());

                    InventoryController inventoryController = fxmlLoader.getController();
                    inventoryController.setHeader(usernameField.getText());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Login Successful");
            } else {
                // reset the fields and set focus to username field
                System.out.println("Login Failed");
                showAlert(Alert.AlertType.ERROR, "Login Error",
                        "Invalid username or password"
                );
                usernameField.setText("");
                passwordField.setText("");
                usernameField.requestFocus();
            }
        } else if (usernameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Username is required");
            usernameField.requestFocus();
        } else if (passwordField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Password is required");
            passwordField.requestFocus();
        }
    }
}
