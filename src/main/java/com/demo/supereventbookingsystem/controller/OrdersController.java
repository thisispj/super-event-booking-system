package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.dao.DatabaseManager;
import com.demo.supereventbookingsystem.model.Event;
import com.demo.supereventbookingsystem.model.Order;
import com.demo.supereventbookingsystem.model.Booking;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class OrdersController implements Initializable {
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, String> orderNumberColumn;
    @FXML
    private TableColumn<Order, String> dateTimeColumn;
    @FXML
    private TableColumn<Order, String> eventsColumn;
    @FXML
    private TableColumn<Order, Double> totalPriceColumn;
    @FXML
    private Button exportOrdersButton;
    @FXML
    private Button backToDashboardButton;

    private MainController mainController;
    private String username;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up table columns
        orderNumberColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrderNumber())
        );
        dateTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getDateTime();
            return new javafx.beans.property.SimpleStringProperty(
                    dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm a"))
            );
        });
        eventsColumn.setCellValueFactory(cellData -> {
            List<Booking> bookings = cellData.getValue().getBookings();
            String events = bookings.stream()
                    .map(booking -> booking.getEvent().getTitle() + " (" + booking.getQuantity() + " seats)")
                    .collect(Collectors.joining(", "));
            return new javafx.beans.property.SimpleStringProperty(events);
        });
        totalPriceColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getTotalPrice()).asObject()
        );

        // Load orders if username is set
        if (username != null) {
            loadOrders();
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setUsername(String username) {
        this.username = username;
        loadOrders();
    }

    private void loadOrders() {
        try {
            List<Order> orders = DatabaseManager.getInstance().getUserOrders(username);
            ordersTable.setItems(FXCollections.observableArrayList(orders));
            ordersTable.refresh();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load orders: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportOrders() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Orders File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("orders.txt");
        Stage stage = (Stage) ordersTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("Super Event Booking System\n");
                writer.write("------------------------\n");
                writer.write(String.format("Order history of: %s\n", this.username));
                writer.write(String.format("Date exported: %s\n", LocalDate.now().toString()));
                writer.write("------------------------\n");
                for (Order order : ordersTable.getItems()) {
                    String events = order.getBookings().stream()
                            .map(booking -> {
                                Event event = booking.getEvent();
                                return String.format(
                                        "Event ID: %s, Title: %s, Venue: %s, Day: %s, Price: $%.2f, Quantity: %d",
                                        event.getEventId(),
                                        event.getTitle(),
                                        event.getVenue(),
                                        event.getDay(),
                                        event.getPrice(),
                                        booking.getQuantity()
                                );
                            })
                            .collect(Collectors.joining("\n"));

                    writer.write(String.format("Order Number: %s\n", order.getOrderNumber()));
                    writer.write(String.format("Date & Time: %s\n", order.getDateTime().toString()));
                    writer.write("Events:\n" + events + "\n");
                    writer.write(String.format("Total Price: $%.2f\n", order.getTotalPrice()));
                    writer.write("------------------------\n");
                }

                showAlert("Success", "Orders exported successfully to " + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Error", "Failed to export orders: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleBack() {
        mainController.showDashboard();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}