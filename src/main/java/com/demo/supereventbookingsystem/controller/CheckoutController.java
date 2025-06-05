package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.dao.DatabaseManager;
import com.demo.supereventbookingsystem.model.Cart;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;

public class CheckoutController {
    @FXML
    private Label totalPriceLabel;
    @FXML
    private TextField confirmationCodeField;
    @FXML
    private Label warningLabel;
    @FXML
    private Label titleLabel;

    private Cart cart;
    private double totalPrice;
    private boolean confirmed = false;

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
        totalPriceLabel.setText(String.format("Total Price: $%.2f", totalPrice));
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    private void handleConfirm() {
        try {
            // Validate seat availability
            String seatValidationError = cart.validateSeatAvailability();
            if (seatValidationError != null) {
                showTemporaryError(seatValidationError);
                return;
            }

            // Validate event dates
            String dateValidationError = DatabaseManager.getInstance().validateEventDates(cart);
            if (dateValidationError != null) {
                showTemporaryError(dateValidationError);
                return;
            }

            // Validate confirmation code
            String confirmationCode = confirmationCodeField.getText();
            if (!isValidConfirmationCode(confirmationCode)) {
                showTemporaryError("Invalid confirmation code. Must be exactly 6 digits.");
                return;
            }

            // If all validations pass, confirm the checkout
            confirmed = true;
            closeStage();
        } catch (SQLException e) {
            showTemporaryError("Error during checkout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) confirmationCodeField.getScene().getWindow();
        stage.close();
    }

    private boolean isValidConfirmationCode(String code) {
        if (code == null || code.length() != 6) {
            return false;
        }
        try {
            Integer.parseInt(code);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showTemporaryError(String errorMessage) {
        warningLabel.setText(errorMessage);

        TranslateTransition shake = new TranslateTransition(Duration.millis(50), warningLabel);
        shake.setByX(5);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();

        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> warningLabel.setText(""));
        pause.play();
    }
}