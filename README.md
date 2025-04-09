# Inventory Management Application

## Overview

The Inventory Management Application is a desktop application for managing inventory, parts, and products.
Users can log in, view and manage inventory, add/edit parts and products, and persist data using an embedded SQLite database. The application is structured using MVC (Model-View-Controller) principles for maintainability and scalability.

## Tech Stack

- **Java 17+**
- **JavaFX** (UI framework)
- **SQLite** (Embedded database)
- **Maven/Gradle** (recommended for dependency management)
- **FXML** (for UI layout)

## Main Components

### 1. Application Entry Point

- **InventoryManagementApplication.java** : Initializes the JavaFX application, loads the main view, and sets up the primary stage.

### 2. Controllers

- **LoginController.java** : Handles user authentication and login logic.
- **InventoryController.java** : Manages the main inventory dashboard, including displaying parts and products.
- **PartsController.java** : Handles CRUD operations for parts (InHouse and Outsourced).
- **ProductsController.java** : Manages product-related operations, including association with parts.

### 3. Model

- **Inventory.java** : Central model holding collections of parts and products.
- **Part.java** : Abstract base class for parts.
- **InHouse.java / Outsourced.java** : Concrete implementations of Part, representing different part types.
- **Product.java** : Represents a product, which can be composed of multiple parts.
- **Login.java** : Represents user credentials and authentication logic.

### 4. Database Integration

- **DatabaseUtil.java** : Utility class for connecting to and interacting with the SQLite database (`inventory.db`). Handles CRUD operations and schema management.

### 5. Utility

- **FileUtil.java** : Handles file operations, such as importing/exporting inventory data.
- **Helper.java** : Provides common helper methods used across the application.

### 6. Views (FXML)

- **main-view.fxml** : Main dashboard layout.
- **inventory-tab.fxml** : Inventory tab UI.
- **part-editor-view.fxml** : UI for adding/editing parts.
- **product-editor-view.fxml** : UI for adding/editing products.

## How Components Interact

- **Controllers** respond to user actions in the UI (defined in FXML files), update the **Model**, and trigger database operations via **DatabaseUtil**.
- **Model** classes represent the application's data and business logic.
- **DatabaseUtil** persists and retrieves data from the SQLite database, ensuring data consistency.
- **Utility** classes support file operations and reusable logic.
- **Views** (FXML) define the UI layout and are loaded by the application entry point and controllers.

## Features

- User authentication (login screen)
- View, add, edit, and delete parts and products
- Support for multiple part types (InHouse, Outsourced)
- Product-part association management
- Persistent storage using SQLite
- Clean, modular codebase following MVC principles
