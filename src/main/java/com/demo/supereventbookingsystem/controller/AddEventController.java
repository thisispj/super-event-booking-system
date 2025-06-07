package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.dao.DatabaseManager;
import com.demo.supereventbookingsystem.model.Event;
import com.demo.supereventbookingsystem.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AddEventController {
    @FXML
    private TextField eventNameField;
    @FXML
    private TextField venueNameField;
    @FXML
    private ComboBox<String> dayComboBox;
    @FXML
    private TextField priceField;
    @FXML
    private TextField capacityField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button addEventButton;

    private MainController mainController;
    private User currentUser;

    @FXML
    private void initialize() {
        dayComboBox.getItems().addAll("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
        cancelButton.setOnAction(e -> handleBack());
        addEventButton.setOnAction(e -> handleAddEvent());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) cancelButton.getScene().getWindow(); // Get the current stage
        stage.close(); // Close the modal window
    }

    @FXML
    private void handleAddEvent() {
        String eventName = eventNameField.getText().trim();
        String venueName = venueNameField.getText().trim();
        String day = dayComboBox.getValue();
        String priceText = priceField.getText().trim();
        String capacityText = capacityField.getText().trim();

        if (eventName.isEmpty() || venueName.isEmpty() || day == null || priceText.isEmpty() || capacityText.isEmpty()) {
            showAlert("Error", "All fields are required.");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int capacity = Integer.parseInt(capacityText);
            if (price < 0 || capacity <= 0) {
                showAlert("Error", "Price must be non-negative and capacity must be positive.");
                return;
            }

            // Check for duplicates
            Event newEvent = new Event(0, eventName, venueName, day, price, 0, capacity, false, false);
            if (DatabaseManager.getInstance().eventExists(newEvent)) {
                showAlert("Error", "An event with the same name, venue, and day already exists.");
                return;
            }

            // Save the event
            DatabaseManager.getInstance().insertEvent(newEvent);
            showAlert("Success", "Event added successfully!");
            Stage stage = (Stage) addEventButton.getScene().getWindow(); // Get the current stage
            stage.close(); // Close the modal window
        } catch (NumberFormatException e) {
            showAlert("Error", "Price and capacity must be valid numbers.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to add event: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}