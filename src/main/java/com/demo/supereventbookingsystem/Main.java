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

    // S4053761, Akshay Panikulam Joy
    // Y Signup
    // Y Login
    // Y Read the events from database and display
    // N Events can be booked and bookings can be modified
    // N Bookings are validated
    // N Checkout is validated with confirmation code and day information
    // N Remaining seats of events is updated once an order is made
    // N User can view all orders
    // N User can export orders to file
    // N Admin GUI & admin login implemented
    // N Admin display implemented (no duplicate event titles)
    // N Event disable function implemented
    // N Event adding & deletion functions implemented
    // N Event modification function implemented
    // N Viewing orders of all users implemented
    // N User password update and encryption implemented
    // N Junit test cases included
    // Y Design pattern (in addition to MVC)
    public static void main(String[] args) {
        launch(args);
    }
}