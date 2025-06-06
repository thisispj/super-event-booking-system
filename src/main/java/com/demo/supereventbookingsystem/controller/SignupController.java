package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.dao.DatabaseManager;
import com.demo.supereventbookingsystem.model.User;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

public class SignupController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField preferredNameField;
    @FXML
    private Label errorLabel;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String preferredName = preferredNameField.getText();
            if (username.isEmpty() || password.isEmpty() || preferredName.isEmpty()) {
                showTemporaryError("All fields are required.");
                return;
            }
            User user = new User(username, password, preferredName, 1); // Default to regular user (user_type_id = 1)
            DatabaseManager.getInstance().saveUser(user);
            mainController.setCurrentUser(user);
            mainController.showDashboard();
        } catch (SQLException e) {
            showTemporaryError("Error: Username already exists or database issue.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/demo/supereventbookingsystem/view/login.fxml"));
            Scene scene = new Scene(loader.load());
            LoginController controller = loader.getController();
            controller.setMainController(mainController);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            showTemporaryError("Error loading login: " + e.getMessage());
        }
    }

    private void showTemporaryError(String errorMessage) {
        if (errorLabel != null) {
            errorLabel.setText(errorMessage);
            errorLabel.setTextFill(Color.web("#a80000"));
            TranslateTransition shake = new TranslateTransition(Duration.millis(50), errorLabel);
            shake.setByX(5);
            shake.setCycleCount(6);
            shake.setAutoReverse(true);
            shake.play();
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> errorLabel.setText(""));
            pause.play();
        }
    }
}