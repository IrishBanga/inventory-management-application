package com.ib.InventoryManagementApplication.utility;

import com.ib.InventoryManagementApplication.model.Inventory;
import com.ib.InventoryManagementApplication.model.Product;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileUtil {

    public static final String FILENAME = "inventory.dat";

    public static void writeToFile(Inventory obj) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILENAME))) {
            out.writeInt(obj.getAllParts().size());
            for (Part part : obj.getAllParts()) {
                out.writeObject(part);
            }
            out.writeInt(obj.getAllProducts().size());
            for (Product product : obj.getAllProducts()) {
                out.writeObject(product);
            }

            System.out.println("Successfully saved to file: " + FILENAME);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static Inventory readFromFile() {
        Inventory obj = new Inventory();
        try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(FILENAME))) {
            int partsSize = oi.readInt();
            for (int i = 0; i < partsSize; i++) {
                obj.addPart((Part) oi.readObject());
            }
            int productsSize = oi.readInt();
            for (int i = 0; i < productsSize; i++) {
                obj.addProduct((Product) oi.readObject());
            }
            System.out.println("Successfully read from file: " + FILENAME);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return obj;
    }
}
