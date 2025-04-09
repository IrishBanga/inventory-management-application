package com.ib.InventoryManagementApplication.controllers;

import com.ib.InventoryManagementApplication.model.InHouse;
import com.ib.InventoryManagementApplication.model.Outsourced;
import com.ib.InventoryManagementApplication.utility.Part;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.function.Consumer;

import static com.ib.InventoryManagementApplication.utility.Helper.*;

public class PartsController {
    private int id = 0;

    @FXML
    private Button cancelPartModificationButton;
    @FXML
    private RadioButton inHouseRadioButton;
    @FXML
    private Label machineOrCompanyNameLabel;
    @FXML
    private TextField machineOrCompanyNameTextField;
    @FXML
    private TextField maxLimitTextField;
    @FXML
    private TextField minLimitTextField;
    @FXML
    private RadioButton outsourcedRadioButton;
    @FXML
    private Label partEditorLabel;
    @FXML
    private TextField partIDTextField;
    @FXML
    private TextField partNameTextField;
    @FXML
    private TextField partPriceTextField;
    @FXML
    private TextField partStockTextField;
    @FXML
    private Button savePartButton;
    private ToggleGroup partTypeToggleGroup;
    private Consumer<Part> returnHandler;

    public void setInitialData(String title, Part part, Consumer<Part> partConsumer) {
        partEditorLabel.setText(title);
        this.returnHandler = partConsumer;
        if (part != null) {
            setPartData(part);
            id = part.getId();
        }
    }

    public void setPartData(Part part) {
        partTypeToggleGroup.selectToggle(part instanceof InHouse ? inHouseRadioButton : outsourcedRadioButton);
        partIDTextField.setText(String.valueOf(part.getId()));
        partNameTextField.setText(part.getName());
        partPriceTextField.setText(String.valueOf(part.getPrice()));
        partStockTextField.setText(String.valueOf(part.getStock()));
        minLimitTextField.setText(String.valueOf(part.getMin()));
        maxLimitTextField.setText(String.valueOf(part.getMax()));
        machineOrCompanyNameTextField.setText(part instanceof InHouse ? String.valueOf(((InHouse) part).getMachine()) :
                ((Outsourced) part).getCompanyName());
        machineOrCompanyNameLabel.setText(
                part instanceof InHouse ? "Machine ID" : "Company Name"
        );
    }

    @FXML
    void initialize() {
        // set-up part type toggle group
        partTypeToggleGroup = new ToggleGroup();
        partTypeToggleGroup.getToggles().addAll(inHouseRadioButton, outsourcedRadioButton);

        // update labels based on selected part type
        partTypeToggleGroup.selectedToggleProperty().addListener((
                observable, oldValue, newValue) -> {
            if (newValue == inHouseRadioButton) {
                machineOrCompanyNameLabel.setText("Machine ID");
            } else {
                machineOrCompanyNameLabel.setText("Company Name");
            }
        });

        // register event handlers
        savePartButton.setOnAction(event -> {
            handleSave();
        });

        cancelPartModificationButton.setOnAction(event -> {
            handleCancel();
        });
    }

    private void handleSave() {
        if (isFieldFilled(partNameTextField) && isFieldFilled(partPriceTextField) &&
                isFieldFilled(machineOrCompanyNameTextField) && partTypeToggleGroup.getSelectedToggle() != null) {
            try {
                String name = partNameTextField.getText();
                double price = Double.parseDouble(partPriceTextField.getText());
                int stock = isFieldFilled(partStockTextField) ?
                        Integer.parseInt(partStockTextField.getText()) : 0;
                int min = isFieldFilled(minLimitTextField) ?
                        Integer.parseInt(minLimitTextField.getText()) : 0;
                int max = isFieldFilled(maxLimitTextField) ?
                        Integer.parseInt(maxLimitTextField.getText()) : 0;

                if (min > max) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input",
                            "Min limit cannot be greater than max limit.");
                } else if (stock < min || stock > max) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input",
                            """
                                    Stock value must be between Min and Max values.
                                    Please define valid boundaries for stock value or clear stock value.
                                    It will default to 0.
                                    """);
                } else {
                    Part part = partTypeToggleGroup.getSelectedToggle() == inHouseRadioButton ?
                            new InHouse(
                                    id == 0 ? generateId() : id,
                                    name, price, stock, min, max, Integer.parseInt(machineOrCompanyNameTextField.getText())
                            ) :
                            new Outsourced(
                                    id == 0 ? generateId() : id,
                                    name, price, stock, min, max, machineOrCompanyNameTextField.getText()
                            );
                    returnHandler.accept(part);
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input",
                        """
                                One or more fields have invalid input.
                                Please enter valid input for all fields.
                                Issue is caused by input:""" + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Incomplete Input",
                    """
                            One or more required fields (Name, Price, Machine ID/Company Name) are empty.
                            Please enter valid input for all fields.""");
        }
    }

    private void handleCancel() {
        showAlertWithCallback(Alert.AlertType.CONFIRMATION,
                "Cancel", "Are you sure you want to cancel?",
                val -> {
                    if (val) {
                        returnHandler.accept(null);
                    }
                }
        );
    }
}
