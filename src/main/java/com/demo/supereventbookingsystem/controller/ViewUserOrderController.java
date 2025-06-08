package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.dao.DatabaseManager;
import com.demo.supereventbookingsystem.model.Order;
import com.demo.supereventbookingsystem.model.User;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ViewUserOrderController implements Initializable {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label availableEventsLabel;
    @FXML
    private ComboBox<String> searchUserComboBox;
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
    private Label preferredNameLabel;
    @FXML
    private Label totalOrdersLabel;
    @FXML
    private Label totalMoneySpentLabel;
    @FXML
    private Label memberSinceLabel;
    @FXML
    private Button closeButton;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize table columns
        orderNumberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderNumber()));
        dateTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateTime().toString()));
        eventsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookingsSummary()));
        totalPriceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalPrice()).asObject());

        // Load normal users into ComboBox
        try {
            List<User> normalUsers = DatabaseManager.getInstance().getAllUsers().stream()
                    .filter(user -> user.getUserTypeId() == 1)
                    .collect(Collectors.toList());
            searchUserComboBox.getItems().addAll(normalUsers.stream()
                    .map(User::getUsername)
                    .collect(Collectors.toList()));
            searchUserComboBox.setOnAction(e -> loadUserOrders());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void loadUserOrders() {
        String selectedUsername = searchUserComboBox.getValue();
        if (selectedUsername == null || selectedUsername.trim().isEmpty()) {
            return;
        }

        try {
            User user = DatabaseManager.getInstance().getUserByUsername(selectedUsername);
            if (user != null) {
                preferredNameLabel.setText("Preferred Name: " + user.getPreferredName());
                memberSinceLabel.setText("Member Since: " + user.getMemberSince().toString());

                List<Order> orders = DatabaseManager.getInstance().getOrdersByUsername(selectedUsername);
                if (orders.isEmpty()) {
                    ordersTable.setItems(FXCollections.observableArrayList());
                    showAlert("Info", "No orders found for " + selectedUsername);
                    totalOrdersLabel.setText("Total Orders: 0");
                    totalMoneySpentLabel.setText("Total Money Spent: $0.0");
                } else {
                    ordersTable.setItems(FXCollections.observableArrayList(orders));
                    long orderCount = orders.size();
                    double totalSpent = orders.stream().mapToDouble(Order::getTotalPrice).sum();
                    totalOrdersLabel.setText("Total Orders: " + orderCount);
                    totalMoneySpentLabel.setText("Total Money Spent: $" + String.format("%.2f", totalSpent));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load orders for " + selectedUsername + ": " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}