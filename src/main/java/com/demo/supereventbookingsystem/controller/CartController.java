package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.model.Cart;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.net.URL;
import java.util.ResourceBundle;

public class CartController implements Initializable {
    @FXML
    private Label cartLabel;
    @FXML
    private TableView<Cart.CartItem> cartTable;
    @FXML
    private TableColumn<Cart.CartItem, String> titleColumn;
    @FXML
    private TableColumn<Cart.CartItem, String> venueColumn;
    @FXML
    private TableColumn<Cart.CartItem, String> dayColumn;
    @FXML
    private TableColumn<Cart.CartItem, Integer> quantityColumn;
    @FXML
    private TableColumn<Cart.CartItem, Double> totalPriceColumn;

    private MainController mainController;
    private Cart cart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEvent().getTitle()));
        venueColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEvent().getVenue()));
        dayColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEvent().getDay()));
        quantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        totalPriceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getTotalPrice()).asObject());

        if (cart != null) {
            cartTable.setItems(FXCollections.observableArrayList(cart.getItems()));
            cartLabel.setText("Your Cart (" + cart.getItems().size() + " items)");
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCartItems(Cart cart) {
        this.cart = cart;
    }

    @FXML
    private void handleBack() {
        mainController.showDashboard();
    }
}