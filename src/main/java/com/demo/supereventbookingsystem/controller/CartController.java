package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.dao.DatabaseManager;
import com.demo.supereventbookingsystem.model.Cart;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CartController implements Initializable {
    @FXML
    private Label cartLabel;
    @FXML
    private Label cartErrorLabel;
    @FXML
    private TableView<Cart.CartItem> cartTable;
    @FXML
    private TableColumn<Cart.CartItem, Integer> eventIdColumn;
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
    @FXML
    private Label selectedEventLabel;
    @FXML
    private TextField quantityField;
    @FXML
    private Label cartTotalLabel;
    @FXML
    private Button plusTicketBtn;
    @FXML
    private Button minusTicketBtn;
    @FXML
    private Button removeEventBtn;
    @FXML
    private Button checkoutBtn;
    @FXML
    private Button backToDasboardBtn;

    private MainController mainController;
    private Cart cart;
    private String username;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up TableView columns
        eventIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getEvent().getEventId()).asObject());
        titleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEvent().getTitle()));
        venueColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEvent().getVenue()));
        dayColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEvent().getDay()));
        quantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        totalPriceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getTotalPrice()).asObject());

        // Initialize UI elements
        if (cart != null) {
            cartTable.setItems(FXCollections.observableArrayList(cart.getItems()));
            cartLabel.setText("Your Cart (" + cart.getItems().size() + " items)");
            updateCartTotal();
        } else {
            cartLabel.setText("Your Cart (0 items)");
            cartTotalLabel.setText("Cart Total: $0.0");
        }

        // Disable buttons and field by default
        plusTicketBtn.setDisable(true);
        minusTicketBtn.setDisable(true);
        removeEventBtn.setDisable(true);
        quantityField.setDisable(true);

        // Handle TableView selection
        cartTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedEventLabel.setText(newSelection.getEvent().getTitle());
                quantityField.setText(String.valueOf(newSelection.getQuantity()));
                plusTicketBtn.setDisable(false);
                minusTicketBtn.setDisable(false);
                removeEventBtn.setDisable(false);
                quantityField.setDisable(false);
            } else {
                selectedEventLabel.setText("None");
                quantityField.clear();
                plusTicketBtn.setDisable(true);
                minusTicketBtn.setDisable(true);
                removeEventBtn.setDisable(true);
                quantityField.setDisable(true);
            }
        });
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCartItems(Cart cart) {
        this.cart = cart;
        if (cartTable != null) {
            cartTable.setItems(FXCollections.observableArrayList(cart.getItems()));
            cartLabel.setText("Your Cart (" + cart.getItems().size() + " items)");
            updateCartTotal();
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    private void handleAddTicket() {
        Cart.CartItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int newQuantity = selectedItem.getQuantity() + 1;
            int availableTickets = selectedItem.getEvent().getAvailableTickets();
            if (newQuantity > availableTickets) {
                showTemporaryError("Cannot add more tickets: only " + availableTickets + " available!");
                return;
            }
            int selectedEventId = selectedItem.getEvent().getEventId();
            updateCartItemQuantity(selectedItem, newQuantity);
            reselectItem(selectedEventId);
        }
    }

    @FXML
    private void handleRemoveTicket() {
        Cart.CartItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int newQuantity = selectedItem.getQuantity() - 1;
            if (newQuantity <= 0) {
                handleRemoveEvent();
            } else {
                int selectedEventId = selectedItem.getEvent().getEventId();
                updateCartItemQuantity(selectedItem, newQuantity);
                reselectItem(selectedEventId);
            }
        }
    }

    @FXML
    private void handleRemoveEvent() {
        Cart.CartItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null && username != null) {
            try {
                int selectedEventId = selectedItem.getEvent().getEventId();
                String sql = "DELETE FROM cart WHERE username = ? AND event_id = ?";
                try (var pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setInt(2, selectedItem.getEvent().getEventId());
                    pstmt.executeUpdate();
                }

                cart = DatabaseManager.getInstance().getCartItems(username);
                cartTable.setItems(FXCollections.observableArrayList(cart.getItems()));
                cartLabel.setText("Your Cart (" + cart.getItems().size() + " items)");
                updateCartTotal();
                // Only clear selection if the item was removed
                if (cart.getItems().stream().noneMatch(item -> item.getEvent().getEventId() == selectedEventId)) {
                    cartTable.getSelectionModel().clearSelection();
                } else {
                    reselectItem(selectedEventId);
                }
            } catch (SQLException e) {
                showTemporaryError("Error removing item: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCheckout() {
        // To be implemented later
    }

    @FXML
    private void handleBack() {
        mainController.showDashboard();
    }

    private void updateCartItemQuantity(Cart.CartItem item, int newQuantity) {
        if (username != null) {
            try {
                String sql = "UPDATE cart SET quantity = ?, total_price = ? WHERE username = ? AND event_id = ?";
                try (var pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
                    pstmt.setInt(1, newQuantity);
                    pstmt.setDouble(2, item.getEvent().getPrice() * newQuantity);
                    pstmt.setString(3, username);
                    pstmt.setInt(4, item.getEvent().getEventId());
                    pstmt.executeUpdate();
                }

                cart = DatabaseManager.getInstance().getCartItems(username);
                cartTable.setItems(FXCollections.observableArrayList(cart.getItems()));
                quantityField.setText(String.valueOf(newQuantity));
                updateCartTotal();
            } catch (SQLException e) {
                showTemporaryError("Error updating quantity: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void updateCartTotal() {
        if (cart != null) {
            double total = cart.getItems().stream().mapToDouble(Cart.CartItem::getTotalPrice).sum();
            cartTotalLabel.setText(String.format("Cart Total: $%.2f", total));
        } else {
            cartTotalLabel.setText("Cart Total: $0.0");
        }
    }

    private void showTemporaryError(String errorMessage) {
        if (cartErrorLabel != null) {
            // Set error message and color
            cartErrorLabel.setText(errorMessage);

            // Create shaking effect using TranslateTransition
            TranslateTransition shake = new TranslateTransition(Duration.millis(50), cartErrorLabel);
            shake.setByX(5); // Move 5 pixels to the right
            shake.setCycleCount(6); // Repeat 6 times (3 full shakes)
            shake.setAutoReverse(true); // Reverse direction each cycle

            // Play the shaking effect
            shake.play();

            // Set up the 3-second timer to revert text and color
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> {
                cartErrorLabel.setText("");
            });
            pause.play();
        }
    }

    private void reselectItem(int eventId) {
        if (cart != null) {
            cartTable.getItems().stream()
                    .filter(item -> item.getEvent().getEventId() == eventId)
                    .findFirst()
                    .ifPresent(item -> cartTable.getSelectionModel().select(item));
        }
    }
}