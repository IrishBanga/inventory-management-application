package com.ib.InventoryManagementApplication.database;

import com.ib.InventoryManagementApplication.model.InHouse;
import com.ib.InventoryManagementApplication.model.Inventory;
import com.ib.InventoryManagementApplication.model.Outsourced;
import com.ib.InventoryManagementApplication.model.Product;
import com.ib.InventoryManagementApplication.utility.Part;
import javafx.collections.ObservableList;

import java.sql.*;

import static com.ib.InventoryManagementApplication.utility.Helper.getProjectPath;

public class DatabaseUtil {

    private static final String DB_CONNECTION = "jdbc:sqlite:" + getProjectPath() + "\\src\\main\\java\\com\\ib\\InventoryManagementApplication\\database\\inventory.db";

    static {
        initializeDatabase();
    }

    private static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_CONNECTION);
             Statement statement = connection.createStatement()) {

            // Create Parts table
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Parts (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "type TEXT NOT NULL CHECK(type IN ('IN_HOUSE', 'OUTSOURCED')), " +
                            "name TEXT NOT NULL, " +
                            "price REAL NOT NULL, " +
                            "stock INTEGER NOT NULL, " +
                            "min INTEGER NOT NULL, " +
                            "max INTEGER NOT NULL, " +
                            "machine_id INTEGER, " +
                            "company_name TEXT)"
            );

            // Create Products table
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Products (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "name TEXT NOT NULL, " +
                            "price REAL NOT NULL, " +
                            "stock INTEGER NOT NULL, " +
                            "min INTEGER NOT NULL, " +
                            "max INTEGER NOT NULL)"
            );

            // Create a table to store associations between Products and Parts
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ProductParts (" +
                            "product_id INTEGER, " +
                            "part_id INTEGER, " +
                            "PRIMARY KEY (product_id, part_id), " +
                            "FOREIGN KEY (product_id) REFERENCES Products(id), " +
                            "FOREIGN KEY (part_id) REFERENCES Parts(id))"
            );

            System.out.println("Database connection established and initialized at: " + DB_CONNECTION);
        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static void saveToDB(Inventory inventory) {
        try (Connection connection = DriverManager.getConnection(DB_CONNECTION)) {
            connection.setAutoCommit(false);

            // Clear existing data
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM ProductParts");
                statement.executeUpdate("DELETE FROM Parts");
                statement.executeUpdate("DELETE FROM Products");
            }

            // Save Parts
            saveParts(connection, inventory.getAllParts());

            // Save Products
            saveProducts(connection, inventory.getAllProducts());

            connection.commit();

            System.out.println("Data saved to database successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("Save operation failed", e);
        }
    }

    private static void saveParts(Connection connection, ObservableList<Part> parts) throws SQLException {
        String sql = "INSERT INTO Parts (id, type, name, price, stock, min, max, machine_id, company_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Part part : parts) {
                statement.setInt(1, part.getId());
                statement.setString(2, part instanceof InHouse ? "IN_HOUSE" : "OUTSOURCED");
                statement.setString(3, part.getName());
                statement.setDouble(4, part.getPrice());
                statement.setInt(5, part.getStock());
                statement.setInt(6, part.getMin());
                statement.setInt(7, part.getMax());

                if (part instanceof InHouse) {
                    statement.setInt(8, ((InHouse) part).getMachine());
                    statement.setNull(9, Types.VARCHAR);
                } else {
                    statement.setNull(8, Types.INTEGER);
                    statement.setString(9, ((Outsourced) part).getCompanyName());
                }
                statement.executeUpdate();
            }
        }
    }

    private static void saveProducts(Connection connection, ObservableList<Product> products) throws SQLException {
        String productSql = "INSERT INTO Products (id, name, price, stock, min, max) VALUES (?, ?, ?, ?, ?, ?)";
        String associationSql = "INSERT INTO ProductParts (product_id, part_id) VALUES (?, ?)";

        try (PreparedStatement productStmt = connection.prepareStatement(productSql/*, Statement.RETURN_GENERATED_KEYS*/);
             PreparedStatement associationStmt = connection.prepareStatement(associationSql)) {

            for (Product product : products) {
                productStmt.setInt(1, product.getId());
                productStmt.setString(2, product.getName());
                productStmt.setDouble(3, product.getPrice());
                productStmt.setInt(4, product.getStock());
                productStmt.setInt(5, product.getMin());
                productStmt.setInt(6, product.getMax());
                productStmt.executeUpdate();

                // Insert associations
                for (Integer partId : product.getAllAssociatedParts()) {
                    associationStmt.setInt(1, product.getId());
                    associationStmt.setInt(2, partId);
                    associationStmt.addBatch();
                }
            }
            associationStmt.executeBatch();
        }
    }

    public static Inventory readFromDB() {
        Inventory inventory = new Inventory();
        try (Connection connection = DriverManager.getConnection(DB_CONNECTION)) {
            // Load Parts
            loadParts(connection, inventory);

            // Load Products
            loadProducts(connection, inventory);

            System.out.println("Data loaded from database successfully.");
        } catch (SQLException e) {
            throw new RuntimeException("Load operation failed", e);
        }
        return inventory;
    }

    private static void loadParts(Connection connection, Inventory inventory) throws SQLException {
        String sql = "SELECT * FROM Parts";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                Part part;
                String type = rs.getString("type");

                if (type.equals("IN_HOUSE")) {
                    part = new InHouse(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getInt("min"),
                            rs.getInt("max"),
                            rs.getInt("machine_id")
                    );
                } else {
                    part = new Outsourced(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getInt("min"),
                            rs.getInt("max"),
                            rs.getString("company_name")
                    );
                }
                inventory.addPart(part);
            }
        }
    }

    private static void loadProducts(Connection connection, Inventory inventory) throws SQLException {
        String productSql = "SELECT * FROM Products";
        String associationSql = "SELECT part_id FROM ProductParts WHERE product_id = ?";

        try (PreparedStatement productStmt = connection.prepareStatement(productSql);
             ResultSet productRs = productStmt.executeQuery();
             PreparedStatement associationStmt = connection.prepareStatement(associationSql)) {

            while (productRs.next()) {
                Product product = new Product(
                        productRs.getInt("id"),
                        productRs.getString("name"),
                        productRs.getDouble("price"),
                        productRs.getInt("stock"),
                        productRs.getInt("min"),
                        productRs.getInt("max")
                );

                // Load associated parts
                associationStmt.setInt(1, product.getId());
                try (ResultSet associationRs = associationStmt.executeQuery()) {
                    while (associationRs.next()) {
                        product.addAssociatedPart(associationRs.getInt("part_id"));
                    }
                }
                inventory.addProduct(product);
            }
        }
    }
}
