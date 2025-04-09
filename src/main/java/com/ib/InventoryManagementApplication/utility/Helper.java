package com.ib.InventoryManagementApplication.utility;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

import java.text.NumberFormat;
import java.util.function.Consumer;

public class Helper {
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance();

    public static void showAlertWithCallback(Alert.AlertType alertType, String header,
                                             String content, Consumer<Boolean> callback) {
        Alert alert = new Alert(alertType);
        alert.setTitle(String.valueOf(alertType));
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
        callback.accept(alert.getResult() == ButtonType.OK);
    }

    public static void showAlert(Alert.AlertType alertType, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(String.valueOf(alertType));
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static int generateId() {
        int min = 1000000;
        int max = 9999999;
        return (int) (Math.random() * (max - min + 1) + min);
    }

    public static String formatCurrency(double value) {
        return CURRENCY.format(value);
    }

    public static boolean isFieldFilled(TextField textField) {
        return !textField.getText().trim().isEmpty();
    }

    public static String getProjectPath() {
        return System.getProperty("user.dir");
    }
}
