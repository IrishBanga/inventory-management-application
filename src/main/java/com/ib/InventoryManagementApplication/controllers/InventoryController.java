package com.ib.InventoryManagementApplication.controllers;

import com.ib.InventoryManagementApplication.model.InHouse;
import com.ib.InventoryManagementApplication.model.Inventory;
import com.ib.InventoryManagementApplication.model.Outsourced;
import com.ib.InventoryManagementApplication.model.Product;
import com.ib.InventoryManagementApplication.utility.Part;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

import static com.ib.InventoryManagementApplication.database.DatabaseUtil.readFromDB;
import static com.ib.InventoryManagementApplication.database.DatabaseUtil.saveToDB;
import static com.ib.InventoryManagementApplication.utility.FileUtil.readFromFile;
import static com.ib.InventoryManagementApplication.utility.FileUtil.writeToFile;
import static com.ib.InventoryManagementApplication.utility.Helper.*;

public class InventoryController {
    private Inventory inventory;
    @FXML
    private Button addPartButton;
    @FXML
    private Button addProductButton;
    @FXML
    private Button deletePartButton;
    @FXML
    private Button deleteProductButton;
    @FXML
    private Button exitButton;
    @FXML
    private AnchorPane inventoryAnchorPane;
    @FXML
    private Button modifyPartButton;
    @FXML
    private Button modifyProductButton;
    @FXML
    private TableColumn<Part, String> partIDColumn;
    @FXML
    private TableColumn<Part, String> partInventoryColumn;
    @FXML
    private TableColumn<Part, String> partNameColumn;
    @FXML
    private TableColumn<Part, String> partPriceColumn;
    @FXML
    private TextField partSearchTextField;
    @FXML
    private TabPane partsProductsTabPane;
    @FXML
    private Tab partsTab;
    @FXML
    private TableColumn<Product, String> productIDColumn;
    @FXML
    private TableColumn<Product, String> productInventoryColumn;
    @FXML
    private TableColumn<Product, String> productNameColumn;
    @FXML
    private TableColumn<Product, String> productPriceColumn;
    @FXML
    private TextField productSearchTextField;
    @FXML
    private Tab productsTab;
    @FXML
    private TableView<Part> partsTable;
    @FXML
    private TableView<Product> productsTable;
    @FXML
    private Label inventoryTabHeaderLabel;

    @FXML
    private Button readFromDBButton;

    @FXML
    private Button readFromFileButton;

    @FXML
    private Button saveToDBButton;

    @FXML
    private Button saveToFileButton;

    private Stage stage;
    private Scene currentScene;

    @FXML
    public void initialize() {
        inventory = new Inventory();
        addDummyData(inventory);

        setUpPartsTable();
        setUpProductsTable();

        partSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            partsTable.setItems(inventory.getAllParts().filtered(part ->
                    part.getName().toLowerCase().contains(newValue.toLowerCase())
                            || String.valueOf(part.getId()).contains(newValue.toLowerCase()))
            );
        });

        productSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            productsTable.setItems(inventory.getAllProducts().filtered(product ->
                    product.getName().toLowerCase().contains(newValue.toLowerCase())
                            || String.valueOf(product.getId()).contains(newValue.toLowerCase()))
            );
        });

        addPartButton.setOnAction(event -> {
            loadAddPartView();
        });
        modifyPartButton.setOnAction(event -> {
            loadModifyPartView();
        });
        addProductButton.setOnAction(event -> {
            loadAddProductView();
        });
        modifyProductButton.setOnAction(event -> {
            loadModifyProductView();
        });
        deletePartButton.setOnAction(event -> {
            handleDeletePart();
        });
        deleteProductButton.setOnAction(event -> {
            handleDeleteProduct();
        });

        readFromDBButton.setOnAction(event -> {
            inventory = readFromDB();
            setUpPartsTable();
            setUpProductsTable();
        });

        readFromFileButton.setOnAction(event -> {
            inventory = readFromFile();
            setUpPartsTable();
            setUpProductsTable();
        });

        saveToDBButton.setOnAction(event -> {
            saveToDB(inventory);
        });

        saveToFileButton.setOnAction(event -> {
            writeToFile(inventory);
        });
    }

    public void setHeader(String userName) {
        inventoryTabHeaderLabel.setText("Inventory Management System - " + userName);
    }

    private void handleDeletePart() {
        showAlertWithCallback(Alert.AlertType.CONFIRMATION, "Delete Part",
                "Are you sure you want to delete the part?",
                ret -> {
                    if (ret) {
                        if (inventory.deletePart(partsTable.getSelectionModel().getSelectedItem())) {
                            System.out.println("Part deleted");
                        } else {
                            System.out.println("Part not deleted");
                            showAlert(Alert.AlertType.INFORMATION, "Part not deleted!", """
                                    Part is associated with one or more products!
                                    Please remove the associated part from the products before deleting the part.
                                    """);
                        }
                    }
                });
    }

    private void handleDeleteProduct() {
        showAlertWithCallback(Alert.AlertType.CONFIRMATION, "Delete Product",
                "Are you sure you want to delete the product?",
                ret -> {
                    if (ret) {
                        if (inventory.deleteProduct(productsTable.getSelectionModel().getSelectedItem())) {
                            System.out.println("Product deleted");
                        } else {
                            System.out.println("Product not deleted");
                            showAlert(Alert.AlertType.ERROR, "Product not deleted!", """
                                    Product has one or more associated parts!
                                    Please remove the associated parts before deleting the product.
                                    """);
                        }
                    }
                });
    }

    private void loadAddProductView() {
        try {
            setStageScene();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ib/InventoryManagementApplication/product-editor-view.fxml"));

            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);

            ProductsController productsController = fxmlLoader.getController();
            productsController.setInitialData("Add Product", null, inventory.getAllParts(),
                    product -> {
                        if (product != null) {
                            inventory.addProduct(product);
                        }
                        stage.setScene(currentScene);
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadModifyProductView() {
        if (productsTable.getSelectionModel().getSelectedIndex() == -1) {
            return;
        }
        try {
            setStageScene();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ib/InventoryManagementApplication/product-editor-view.fxml"));

            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);

            ProductsController productsController = fxmlLoader.getController();
            int index = productsTable.getSelectionModel().getSelectedIndex();
            productsController.setInitialData("Modify Product", inventory.getAllProducts().get(index),
                    inventory.getAllParts(),
                    product -> {
                        if (product != null) {
                            inventory.updateProduct(index, product);
                        }
                        stage.setScene(currentScene);
                    });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadAddPartView() {
        try {
            setStageScene();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ib/InventoryManagementApplication/part-editor-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);

            PartsController partsController = fxmlLoader.getController();
            partsController.setInitialData("Add Part", null, part -> {
                if (part != null) {
                    inventory.addPart(part);
                }
                stage.setScene(currentScene);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadModifyPartView() {
        if (partsTable.getSelectionModel().getSelectedIndex() == -1) {
            return;
        }
        try {
            setStageScene();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ib/InventoryManagementApplication/part-editor-view.fxml"));

            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);

            PartsController partsController = fxmlLoader.getController();
            int index = partsTable.getSelectionModel().getSelectedIndex();
            partsController.setInitialData("Modify Part", inventory.getAllParts().get(index),
                    part -> {
                        if (part != null) {
                            inventory.updatePart(index, part);
                        }
                        stage.setScene(currentScene);
                    });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setStageScene() {
        stage = (Stage) inventoryTabHeaderLabel.getScene().getWindow();
        currentScene = inventoryTabHeaderLabel.getScene();
    }

    private void setUpPartsTable() {
        partIDColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                String.valueOf(cv.getValue().getId())
        ));
        partNameColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                cv.getValue().getName()
        ));
        partPriceColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                String.valueOf(cv.getValue().getPrice())
        ));
        partInventoryColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                String.valueOf(cv.getValue().getStock())
        ));
        partsTable.setItems(inventory.getAllParts());
    }

    private void setUpProductsTable() {
        productIDColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                String.valueOf(cv.getValue().getId())
        ));
        productNameColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                cv.getValue().getName()
        ));
        productPriceColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                formatCurrency(cv.getValue().getPrice())
        ));
        productInventoryColumn.setCellValueFactory(cv -> new SimpleStringProperty(
                String.valueOf(cv.getValue().getStock())
        ));
        productsTable.setItems(inventory.getAllProducts());
    }

    public void closeWindow() {
        ((Stage) inventoryAnchorPane.getScene().getWindow()).close();
    }

    // function to add dummy data to the inventory
    private void addDummyData(Inventory inventory) {
        Product dummyProduct1 = new Product(1, "Product 1", 100.0, 10, 1, 100);
        Product dummyProduct2 = new Product(2, "Product 2", 200.0, 20, 1, 200);
        Product dummyProduct3 = new Product(3, "Product 3", 300.0, 30, 1, 300);
        Product dummyProduct4 = new Product(4, "Product 4", 400.0, 40, 1, 400);
        Product dummyProduct5 = new Product(5, "Product 5", 500.0, 50, 1, 500);
        Product dummyProduct6 = new Product(6, "Product 6", 600.0, 60, 1, 600);
        Product dummyProduct7 = new Product(7, "Product 7", 700.0, 70, 1, 700);
        Product dummyProduct8 = new Product(8, "Product 8", 800.0, 80, 1, 800);
        Product dummyProduct9 = new Product(9, "Product 9", 900.0, 90, 1, 900);
        Product dummyProduct10 = new Product(10, "Product 10", 1000.0, 100, 1, 1000);

        Part dummyPart1 = new InHouse(1, "Part 1", 10.0, 10, 1, 100, 1000);
        Part dummyPart2 = new InHouse(2, "Part 2", 20.0, 20, 1, 200, 2000);
        Part dummyPart3 = new InHouse(3, "Part 3", 30.0, 30, 1, 300, 3000);
        Part dummyPart4 = new Outsourced(4, "Part 4", 40.0, 40, 1, 400, "Company 1");
        Part dummyPart5 = new Outsourced(5, "Part 5", 50.0, 50, 1, 500, "Company 2");

        inventory.addPart(dummyPart1);
        inventory.addPart(dummyPart2);
        inventory.addPart(dummyPart3);
        inventory.addPart(dummyPart4);
        inventory.addPart(dummyPart5);

        dummyProduct1.addAssociatedPart(dummyPart1.getId());
        dummyProduct1.addAssociatedPart(dummyPart2.getId());
        dummyProduct1.addAssociatedPart(dummyPart3.getId());

        dummyProduct2.addAssociatedPart(dummyPart4.getId());
        dummyProduct3.addAssociatedPart(dummyPart5.getId());
        dummyProduct4.addAssociatedPart(dummyPart1.getId());

        dummyProduct5.addAssociatedPart(dummyPart4.getId());
        dummyProduct5.addAssociatedPart(dummyPart3.getId());
        dummyProduct5.addAssociatedPart(dummyPart2.getId());

        dummyProduct6.addAssociatedPart(dummyPart1.getId());
        dummyProduct7.addAssociatedPart(dummyPart2.getId());
        dummyProduct8.addAssociatedPart(dummyPart3.getId());
        dummyProduct9.addAssociatedPart(dummyPart4.getId());
        dummyProduct10.addAssociatedPart(dummyPart5.getId());

        inventory.addProduct(dummyProduct1);
        inventory.addProduct(dummyProduct2);
        inventory.addProduct(dummyProduct3);
        inventory.addProduct(dummyProduct4);
        inventory.addProduct(dummyProduct5);
        inventory.addProduct(dummyProduct6);
        inventory.addProduct(dummyProduct7);
        inventory.addProduct(dummyProduct8);
        inventory.addProduct(dummyProduct9);
        inventory.addProduct(dummyProduct10);
    }
}
