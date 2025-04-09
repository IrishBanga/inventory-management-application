package com.ib.InventoryManagementApplication.controllers;

import com.ib.InventoryManagementApplication.model.Product;
import com.ib.InventoryManagementApplication.utility.Part;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ib.InventoryManagementApplication.utility.Helper.*;

public class ProductsController {
    private final ObservableList<Part> associatedPartsObvList = FXCollections.observableArrayList();
    private final ObservableList<Part> parts = FXCollections.observableArrayList();
    @FXML
    private Button addPartToProductButton;
    @FXML
    private Button removeAssociatedPartButton;
    @FXML
    private TableColumn<Part, String> associatedPartIDColumn;
    @FXML
    private TableColumn<Part, String> associatedPartInventoryColumn;
    @FXML
    private TableColumn<Part, String> associatedPartNameColumn;
    @FXML
    private TableColumn<Part, String> associatedPartPriceColumn;
    @FXML
    private TextField associatedPartSearchProductEditorTextField;
    @FXML
    private Button cancelProductModificationButton;
    @FXML
    private TextField maxLimitProductTextField;
    @FXML
    private TextField minLimitProductTextField;
    @FXML
    private TableColumn<Part, String> partIDColumn;
    @FXML
    private TableColumn<Part, String> partInventoryColumn;
    @FXML
    private TableColumn<Part, String> partNameColumn;
    @FXML
    private TableColumn<Part, String> partPriceColumn;
    @FXML
    private TextField partSearchProductEditorTextField;
    @FXML
    private Label productEditorLabel;
    @FXML
    private TextField productIDTextField;
    @FXML
    private TextField productNameTextField;
    @FXML
    private TextField productPriceTextField;
    @FXML
    private TextField productStockTextField;
    @FXML
    private Button saveProductButton;
    @FXML
    private TableView<Part> partsTable;
    @FXML
    private TableView<Part> associatedPartsTable;
    private Consumer<Product> returnHandler;
    private Product product;

    public void setInitialData(String title, Product product, List<Part> parts, Consumer<Product> productConsumer) {
        productEditorLabel.setText(title);

        this.parts.setAll(parts);
        this.returnHandler = productConsumer;

        if (product != null) {
            this.product = product;
            associatedPartsObvList.setAll(parts.stream().filter(part ->
                    product.getAllAssociatedParts().contains(part.getId())).collect(Collectors.toList()));
            setProductData(product);
        }
    }

    private void setProductData(Product product) {
        productIDTextField.setText(String.valueOf(product.getId()));
        productNameTextField.setText(product.getName());
        productPriceTextField.setText(String.valueOf(product.getPrice()));
        productStockTextField.setText(String.valueOf(product.getStock()));
        minLimitProductTextField.setText(String.valueOf(product.getMin()));
        maxLimitProductTextField.setText(String.valueOf(product.getMax()));
    }

    @FXML
    void initialize() {
        saveProductButton.setOnAction(event -> {
            handleSave();
        });

        cancelProductModificationButton.setOnAction(event -> {
            handleCancel();
        });

        partSearchProductEditorTextField.textProperty().addListener((
                observable, oldValue, newValue) -> {
            partsTable.setItems(FXCollections.observableArrayList(parts).filtered(
                    part -> part.getName().toLowerCase().contains(newValue.toLowerCase())
                            || String.valueOf(part.getId()).contains(newValue.toLowerCase())
            ));
        });
        associatedPartSearchProductEditorTextField.textProperty().addListener((
                observable, oldValue, newValue) -> {
            associatedPartsTable.setItems(associatedPartsObvList.filtered(
                    part -> part.getName().toLowerCase().contains(newValue.toLowerCase())
                            || String.valueOf(part.getId()).contains(newValue.toLowerCase())
            ));
        });

        addPartToProductButton.setOnAction(event -> {
            handleAdd();
        });

        removeAssociatedPartButton.setOnAction(event -> {
            handleRemove();
        });

        setupPartsTables();
    }

    private void setupPartsTables() {
        partIDColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                String.valueOf(cv.getValue().getId())
        ));
        partNameColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                cv.getValue().getName()
        ));
        partPriceColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                formatCurrency(cv.getValue().getPrice())
        ));
        partInventoryColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                String.valueOf(cv.getValue().getStock())
        ));

        partsTable.setItems(parts);

        associatedPartIDColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                String.valueOf(cv.getValue().getId())
        ));
        associatedPartNameColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                cv.getValue().getName()
        ));
        associatedPartPriceColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                formatCurrency(cv.getValue().getPrice())
        ));
        associatedPartInventoryColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                String.valueOf(cv.getValue().getStock())
        ));

        associatedPartsTable.setItems(associatedPartsObvList);
    }

    private void handleAdd() {
        Part selectedPart = partsTable.getSelectionModel().getSelectedItem();
        if (selectedPart != null && !associatedPartsObvList.contains(selectedPart)) {
            associatedPartsObvList.add(selectedPart);
            associatedPartsTable.setItems(associatedPartsObvList);
        }
    }

    private void handleRemove() {
        Part selectedPart = associatedPartsTable.getSelectionModel().getSelectedItem();
        if (selectedPart != null) {
            associatedPartsObvList.remove(selectedPart);
            associatedPartsTable.setItems(associatedPartsObvList);
        }
    }

    private void handleSave() {
        if (isFieldFilled(productNameTextField) && isFieldFilled(productPriceTextField)
        ) {
            try {
                String name = productNameTextField.getText();
                double price = Double.parseDouble(productPriceTextField.getText());
                int stock = isFieldFilled(productStockTextField) ?
                        Integer.parseInt(productStockTextField.getText()) : 0;
                int min = isFieldFilled(minLimitProductTextField) ?
                        Integer.parseInt(minLimitProductTextField.getText()) : 0;
                int max = isFieldFilled(maxLimitProductTextField) ?
                        Integer.parseInt(maxLimitProductTextField.getText()) : 0;
                double partsPrice = associatedPartsObvList.stream().mapToDouble(Part::getPrice).sum();

                if (min > max) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input",
                            "Min value cannot be greater than Max value.");
                } else if (stock < min || stock > max) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input",
                            "Stock value must be between Min and Max values.");
                } else if (price < partsPrice) {
                    showAlert(Alert.AlertType.ERROR,
                            "Invalid Input",
                            "Product price cannot be less than the sum of the prices of the associated parts." +
                                    "It must be greater than or equal to : " + formatCurrency(partsPrice));
                } else if (product == null & associatedPartsObvList.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR,
                            "Invalid Input",
                            """
                                    A new product must have at least one part to be saved.
                                    Please add at least one part to the product.""");
                } else {
                    if (product == null) {
                        product = new Product(generateId(), name, price, stock, min, max);
                    } else {
                        product.setName(name);
                        product.setPrice(price);
                        product.setStock(stock);
                        product.setMin(min);
                        product.setMax(max);
                    }
                    product.getAllAssociatedParts().clear();
                    product.getAllAssociatedParts().addAll(
                            associatedPartsObvList.stream().map(Part::getId).collect(Collectors.toSet()));
                    returnHandler.accept(product);
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input",
                        """
                                One or more fields have invalid input.
                                Please enter valid input for all fields.
                                Issue is caused by input:""" + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR,
                    "Incomplete Input",
                    """
                            One or more required fields (Name, Price) are empty.
                            Please enter valid input for all fields.""");
        }
    }

    private void handleCancel() {
        showAlertWithCallback(Alert.AlertType.CONFIRMATION,
                "Cancel Product Modification",
                "Are you sure you want to cancel?",
                val -> {
                    if (val) {
                        returnHandler.accept(null);
                    }
                });
    }
}
