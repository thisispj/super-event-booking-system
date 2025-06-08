package com.demo.supereventbookingsystem;

import com.demo.supereventbookingsystem.controller.MainController;
import com.demo.supereventbookingsystem.dao.DatabaseManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.SQLException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Initialize DatabaseManager to ensure events.dat is loaded
        try {
            DatabaseManager.getInstance();
            System.out.println("DatabaseManager initialized at startup.");
        } catch (Exception e) {
            System.err.println("Failed to initialize DatabaseManager: " + e.getMessage());
            e.printStackTrace();
        }
        MainController mainController = new MainController(primaryStage);
        mainController.showLogin();
    }

    @Override
    public void stop() {
        try {
            DatabaseManager.getInstance().closeConnection();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}