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

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (DatabaseManager.getInstance().validateUser(username, password)) {
                User user = DatabaseManager.getInstance().getUser(username);
                mainController.setCurrentUser(user);
                mainController.showDashboard();
            } else {
                showTemporaryError("Invalid username or password.");
            }
        } catch (SQLException e) {
            showTemporaryError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/demo/supereventbookingsystem/view/signup.fxml"));
            Scene scene = new Scene(loader.load());
            SignupController controller = loader.getController();
            controller.setMainController(mainController);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            showTemporaryError("Error loading signup: " + e.getMessage());
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
            pause.setOnFinished(e -> {
                errorLabel.setText("");
            });
            pause.play();
        }
    }
}