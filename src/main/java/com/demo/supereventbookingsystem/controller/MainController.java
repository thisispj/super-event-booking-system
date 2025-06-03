package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.model.User;
import com.demo.supereventbookingsystem.model.Cart;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {
    private final Stage primaryStage;
    private User currentUser;

    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/demo/supereventbookingsystem/view/login.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setMainController(this);
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Super Event Booking - Login");
            primaryStage.show();
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/demo/supereventbookingsystem/view/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setMainController(this);
            controller.setCurrentUser(currentUser);
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Super Event Booking - Dashboard");
            primaryStage.show();
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showCart(Cart cart) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/demo/supereventbookingsystem/view/cart.fxml"));
            Parent root = loader.load();
            CartController controller = loader.getController();
            controller.setMainController(this);
            controller.setCartItems(cart);
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Super Event Booking - Cart");
            primaryStage.show();
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}