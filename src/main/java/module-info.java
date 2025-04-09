module com.ib.InventoryManagementApplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.ib.InventoryManagementApplication.controllers to javafx.fxml;
    opens com.ib.InventoryManagementApplication to javafx.fxml;
    exports com.ib.InventoryManagementApplication;
}
