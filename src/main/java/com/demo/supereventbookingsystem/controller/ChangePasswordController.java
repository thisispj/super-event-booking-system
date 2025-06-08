package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.dao.DatabaseManager;
import com.demo.supereventbookingsystem.model.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ChangePasswordController implements Initializable {
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button changePasswordButton;

    private MainController mainController;
    private User currentUser;
    private DatabaseManager databaseManager;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        databaseManager = DatabaseManager.getInstance();

        changePasswordButton.setOnAction(event -> handleChangePassword());
        cancelButton.setOnAction(event -> handleCancel());
    }

    private void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();

        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            showAlert("Error", "Current and new passwords cannot be empty.");
            return;
        }

        if (currentPassword.equals(newPassword)) {
            showAlert("Error", "Current and new passwords cannot be the same.");
            return;
        }

        try {
            // Encrypt the entered current password for comparison
            String encryptedCurrent = databaseManager.encryptPassword(currentPassword);
            // Fetch the stored encrypted password for the user
            User user = databaseManager.getUserPassword(currentUser.getUsername());
            if (user == null) {
                showAlert("Error", "User not found.");
                return;
            }
            String storedPassword = user.getPassword();

            // Compare encrypted values
            if (!encryptedCurrent.equals(storedPassword)) {
                showAlert("Error", "Current password is incorrect.");
                return;
            }

            // Encrypt and update the new password
            String encryptedNew = databaseManager.encryptPassword(newPassword);
            databaseManager.updatePassword(currentUser.getUsername(), encryptedNew);

            // Show success message and return to dashboard
            showAlert("Success", "Password Updated Successfully");
            Stage stage = (Stage) changePasswordButton.getScene().getWindow();
            stage.close();
            mainController.showDashboard(); // Return to user dashboard
        } catch (SQLException e) {
            showAlert("Error", "Failed to update password: " + e.getMessage());
        }
    }

    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        mainController.showDashboard(); // Return to user dashboard
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}