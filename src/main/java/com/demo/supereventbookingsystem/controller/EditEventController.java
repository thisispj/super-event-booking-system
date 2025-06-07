package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.dao.DatabaseManager;
import com.demo.supereventbookingsystem.model.Event;
import com.demo.supereventbookingsystem.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.sql.SQLException;

public class EditEventController {
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
    private Button saveButton;

    private MainController mainController;
    private User currentUser;
    private Event selectedEvent;
    private String originalEventName;
    private String originalVenueName;
    private String originalDay;
    private double originalPrice;
    private int originalCapacity;

    @FXML
    private void initialize() {
        dayComboBox.getItems().addAll("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
        cancelButton.setOnAction(e -> handleBack());
        saveButton.setOnAction(e -> handleSave());
        saveButton.setDisable(true);

        // Set up change listeners to enable save button on changes
        eventNameField.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState());
        venueNameField.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState());
        dayComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState());
        priceField.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState());
        capacityField.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState());

        if (selectedEvent != null) {
            prefillFields();
            fetchOriginalEventDetails();
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setSelectedEvent(Event event) {
        this.selectedEvent = event;
        if (selectedEvent != null) {
            prefillFields();
            fetchOriginalEventDetails();
        }
    }

    private void prefillFields() {
        if (selectedEvent != null) {
            eventNameField.setText(selectedEvent.getTitle());
            venueNameField.setText(selectedEvent.getVenue());
            dayComboBox.setValue(selectedEvent.getDay());
            priceField.setText(String.valueOf(selectedEvent.getPrice()));
            capacityField.setText(String.valueOf(selectedEvent.getTotalTickets()));
        }
    }

    private void fetchOriginalEventDetails() {
        if (selectedEvent != null) {
            try {
                Event dbEvent = DatabaseManager.getInstance().getEventById(selectedEvent.getEventId());
                if (dbEvent != null) {
                    originalEventName = dbEvent.getTitle();
                    originalVenueName = dbEvent.getVenue();
                    originalDay = dbEvent.getDay();
                    originalPrice = dbEvent.getPrice();
                    originalCapacity = dbEvent.getTotalTickets();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to fetch original event details: " + e.getMessage());
            }
        }
    }

    private void updateSaveButtonState() {
        if (selectedEvent != null) {
            boolean hasChanges = !eventNameField.getText().trim().equals(originalEventName) ||
                    !venueNameField.getText().trim().equals(originalVenueName) ||
                    !dayComboBox.getValue().equals(originalDay) ||
                    !priceField.getText().trim().equals(String.valueOf(originalPrice)) ||
                    !capacityField.getText().trim().equals(String.valueOf(originalCapacity));
            saveButton.setDisable(!hasChanges);
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSave() {
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
            if (price < 0) {
                showAlert("Error", "Price must be non-negative.");
                return;
            }
            if (capacity <= 0) {
                showAlert("Error", "Capacity must be positive.");
                return;
            }

            Event newEvent = new Event(selectedEvent.getEventId(), eventName, venueName, day, price, selectedEvent.getSoldTickets(), capacity, selectedEvent.isDisabled(), selectedEvent.isDeleted());
            // Fetch current event from DB to compare
            Event currentEvent = DatabaseManager.getInstance().getEventById(selectedEvent.getEventId());
            if (currentEvent != null) {
                boolean noChanges = currentEvent.getTitle().equals(eventName) &&
                        currentEvent.getVenue().equals(venueName) &&
                        currentEvent.getDay().equals(day) &&
                        currentEvent.getPrice() == price &&
                        currentEvent.getTotalTickets() == capacity;
                if (noChanges) {
                    showAlert("Info", "No changes made.");
                } else {
                    // Check for duplicates excluding the current event
                    Event tempEvent = new Event(0, eventName, venueName, day, price, 0, capacity, false, false);
                    if (DatabaseManager.getInstance().eventExists(tempEvent) &&
                            !eventName.equals(currentEvent.getTitle()) &&
                            !venueName.equals(currentEvent.getVenue()) &&
                            !day.equals(currentEvent.getDay())) {
                        showAlert("Error", "An event with the same name, venue, and day already exists.");
                        return;
                    }
                    DatabaseManager.getInstance().updateEvent(newEvent);
                    showAlert("Success", "Event updated successfully!");
                }
            }
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (NumberFormatException e) {
            showAlert("Error", "Price and capacity must be valid numbers.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save event: " + e.getMessage());
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