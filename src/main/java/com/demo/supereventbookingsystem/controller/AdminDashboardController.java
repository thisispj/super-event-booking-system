package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;

public class AdminDashboardController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label availableEventsLabel;
    @FXML
    private Label selectedEventLabel;
    @FXML
    private Button editEventButton;
    @FXML
    private Button deleteEventButton;
    @FXML
    private Button addEventButton;
    @FXML
    private Button viewUserOrdersButton;
    @FXML
    private Button logoutButton;
    @FXML
    private TreeView<?> eventTreeView;

    private MainController mainController;
    private User currentUser;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome Admin, " + (user != null ? user.getPreferredName() : "") + "!");
        }
    }

    @FXML
    private void handleEditEvent() {
        // To be implemented later
    }

    @FXML
    private void handleDeleteEvent() {
        // To be implemented later
    }

    @FXML
    private void handleAddEvent() {
        // To be implemented later
    }

    @FXML
    private void handleViewUserOrders() {
        // To be implemented later
    }

    @FXML
    private void handleLogout() {
        mainController.setCurrentUser(null);
        mainController.showLogin();
    }
}