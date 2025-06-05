package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.dao.DatabaseManager;
import com.demo.supereventbookingsystem.model.Cart;
import com.demo.supereventbookingsystem.model.Event;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CartController implements Initializable {
    @FXML
    private Label cartLabel;
    @FXML
    private Label cartErrorLabel;
    @FXML
    private TableView<Event> cartTable;
    @FXML
    private TableColumn<Event, Integer> eventIdColumn;
    @FXML
    private TableColumn<Event, String> titleColumn;
    @FXML
    private TableColumn<Event, String> venueColumn;
    @FXML
    private TableColumn<Event, String> dayColumn;
    @FXML
    private TableColumn<Event, Integer> quantityColumn;
    @FXML
    private TableColumn<Event, Double> totalPriceColumn;
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
        eventIdColumn.setCellFactory(column -> new TableCell<Event, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    System.out.println("Event ID cell: empty");
                } else {
                    Event event = getTableRow().getItem();
                    setText(String.valueOf(event.getEventId()));
                    setTextFill(Color.BLACK);
                    System.out.println("Event ID cell set to: " + event.getEventId());
                }
            }
        });

        titleColumn.setCellFactory(column -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    System.out.println("Title cell: empty");
                } else {
                    Event event = getTableRow().getItem();
                    setText(event.getTitle());
                    setTextFill(Color.BLACK);
                    System.out.println("Title cell set to: " + event.getTitle());
                }
            }
        });

        venueColumn.setCellFactory(column -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    System.out.println("Venue cell: empty");
                } else {
                    Event event = getTableRow().getItem();
                    setText(event.getVenue());
                    setTextFill(Color.BLACK);
                    System.out.println("Venue cell set to: " + event.getVenue());
                }
            }
        });

        dayColumn.setCellFactory(column -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Event event = getTableRow().getItem();
                    setText(event.getDay());
                    setTextFill(Color.BLACK);
                }
            }
        });

        quantityColumn.setCellFactory(column -> new TableCell<Event, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null || cart == null) {
                    setText(null);
                } else {
                    Event event = getTableRow().getItem();
                    Integer quantity = cart.getItems().get(event.getEventId());
                    setText(String.valueOf(quantity != null ? quantity : 0));
                    setTextFill(Color.BLACK);
                }
            }
        });

        totalPriceColumn.setCellFactory(column -> new TableCell<Event, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null || cart == null) {
                    setText(null);
                } else {
                    Event event = getTableRow().getItem();
                    Integer quantity = cart.getItems().get(event.getEventId());
                    double totalPrice = (quantity != null ? quantity : 0) * (event != null ? event.getPrice() : 0.0);
                    setText(String.format("%.2f", totalPrice));
                    setTextFill(Color.BLACK);
                }
            }
        });

        if (cart != null) {
            updateTableView();
            cartLabel.setText("Your Cart (" + cart.getItemCount() + " items)");
            updateCartTotal();
            updateCheckoutButtonState(); // Update Checkout button state based on cart
        } else {
            cartLabel.setText("Your Cart (0 items)");
            cartTotalLabel.setText("Cart Total: $0.0");
            if (checkoutBtn != null) {
                checkoutBtn.setDisable(true); // Disable Checkout button if cart is null
            }
        }

        plusTicketBtn.setDisable(true);
        minusTicketBtn.setDisable(true);
        removeEventBtn.setDisable(true);
        quantityField.setDisable(true);

        removeEventBtn.setOnAction(event -> handleRemoveEvent());

        cartTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedEventLabel.setText(newSelection.getTitle());
                Integer quantity = cart != null ? cart.getItems().get(newSelection.getEventId()) : 0;
                quantityField.setText(String.valueOf(quantity != null ? quantity : 0));
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
            updateTableView();
            cartLabel.setText("Your Cart (" + cart.getItemCount() + " items)");
            updateCartTotal();
            updateCheckoutButtonState(); // Update Checkout button state based on cart
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    private void handleAddTicket() {
        Event selectedEvent = cartTable.getSelectionModel().getSelectedItem();
        if (selectedEvent != null) {
            Integer currentQuantity = cart.getItems().get(selectedEvent.getEventId());
            int newQuantity = (currentQuantity != null ? currentQuantity : 0) + 1;
            int availableTickets = selectedEvent.getAvailableTickets();
            if (newQuantity > availableTickets) {
                showTemporaryError("Cannot add more tickets: only " + availableTickets + " available!");
                return;
            }
            int selectedEventId = selectedEvent.getEventId();
            updateCartItemQuantity(selectedEvent, newQuantity);
            reselectItem(selectedEventId);
        }
    }

    @FXML
    private void handleRemoveTicket() {
        Event selectedEvent = cartTable.getSelectionModel().getSelectedItem();
        if (selectedEvent != null) {
            Integer currentQuantity = cart.getItems().get(selectedEvent.getEventId());
            int newQuantity = (currentQuantity != null ? currentQuantity : 0) - 1;
            if (newQuantity <= 0) {
                handleRemoveEvent();
            } else {
                int selectedEventId = selectedEvent.getEventId();
                updateCartItemQuantity(selectedEvent, newQuantity);
                reselectItem(selectedEventId);
            }
        }
    }

    @FXML
    private void handleRemoveEvent() {
        Event selectedEvent = cartTable.getSelectionModel().getSelectedItem();
        if (selectedEvent != null && username != null) {
            try {
                int selectedEventId = selectedEvent.getEventId();
                String sql = "DELETE FROM cart WHERE username = ? AND event_id = ?";
                try (var pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setInt(2, selectedEventId);
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected == 0) {
                        showTemporaryError("No item found to remove.");
                        return;
                    }
                }

                cart = DatabaseManager.getInstance().getCartItems(username);
                updateTableView();
                cartLabel.setText("Your Cart (" + cart.getItemCount() + " items)");
                updateCartTotal();
                updateCheckoutButtonState(); // Update Checkout button state after removal
                if (!cart.getItems().containsKey(selectedEventId)) {
                    cartTable.getSelectionModel().clearSelection();
                } else {
                    reselectItem(selectedEventId);
                }
            } catch (SQLException e) {
                showTemporaryError("Error removing item: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showTemporaryError("Please select an event to remove.");
        }
    }

    @FXML
    private void handleCheckout() {
        if (cart == null || cart.getItemCount() == 0) {
            showTemporaryError("Cart is empty!");
            return;
        }

        try {
            double totalPrice = cart.getTotalPrice();

            // Show price confirmation using Alert dialog
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Price Confirmation");
            confirmationAlert.setHeaderText("Total Price: $" + String.format("%.2f", totalPrice));
            confirmationAlert.setContentText("Do you want to proceed with this total?");
            if (confirmationAlert.showAndWait().get() != ButtonType.OK) {
                return; // User cancelled, exit the method
            }

            // Load the custom checkout scene for confirmation code
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/demo/supereventbookingsystem/view/checkout.fxml"));
            Scene checkoutScene = new Scene(loader.load(), 400, 300);
            CheckoutController checkoutController = loader.getController();
            checkoutController.setTotalPrice(totalPrice);
            checkoutController.setCart(cart);

            Stage checkoutStage = new Stage();
            checkoutStage.setTitle("Checkout");
            checkoutStage.initModality(Modality.APPLICATION_MODAL); // Make it modal
            checkoutStage.setScene(checkoutScene);
            checkoutStage.showAndWait(); // Wait for the user to close the stage

            // Proceed based on the user's action
            if (checkoutController.isConfirmed()) {
                // Proceed with checkout
                DatabaseManager.getInstance().updateSoldTicketsAfterCheckout(cart);
                DatabaseManager.getInstance().clearCart(username);

                // Show success message in a dialog box
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Order Success");
                successAlert.setHeaderText("Thank you for your purchase!");
                successAlert.setContentText("Your tickets have been booked. Total: $" + String.format("%.2f", totalPrice));
                successAlert.showAndWait();

                // Clear the cart and update UI
                cart.clear();
                updateTableView();
                cartLabel.setText("Your Cart (0 items)");
                updateCartTotal();
                updateCheckoutButtonState(); // Update Checkout button state after checkout
                cartTable.getSelectionModel().clearSelection();
            } else {
                // Checkout was cancelled, no action needed
                System.out.println("Checkout cancelled by user.");
            }

        } catch (SQLException e) {
            showTemporaryError("Checkout failed: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showTemporaryError("Error loading checkout scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        mainController.showDashboard();
    }

    private void updateCartItemQuantity(Event event, int newQuantity) {
        if (username != null) {
            try {
                int availableTickets = event.getAvailableTickets();
                if (newQuantity > availableTickets) {
                    showTemporaryError("Cannot update quantity: only " + availableTickets + " tickets available!");
                    return;
                }

                Cart tempCart = new Cart();
                tempCart.addItem(event, newQuantity);
                String dateValidationError = DatabaseManager.getInstance().validateEventDates(tempCart);
                if (dateValidationError != null) {
                    showTemporaryError(dateValidationError);
                    return;
                }

                String sql = "UPDATE cart SET quantity = ?, total_price = ? WHERE username = ? AND event_id = ?";
                try (var pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
                    pstmt.setInt(1, newQuantity);
                    pstmt.setDouble(2, event.getPrice() * newQuantity);
                    pstmt.setString(3, username);
                    pstmt.setInt(4, event.getEventId());
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected == 0) {
                        sql = "INSERT INTO cart (username, event_id, quantity, total_price) VALUES (?, ?, ?, ?)";
                        try (var insertPstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
                            insertPstmt.setString(1, username);
                            insertPstmt.setInt(2, event.getEventId());
                            insertPstmt.setInt(3, newQuantity);
                            insertPstmt.setDouble(4, event.getPrice() * newQuantity);
                            insertPstmt.executeUpdate();
                        }
                    }
                }

                cart = DatabaseManager.getInstance().getCartItems(username);
                updateTableView();
                quantityField.setText(String.valueOf(newQuantity));
                updateCartTotal();
                updateCheckoutButtonState(); // Update Checkout button state after quantity change
            } catch (SQLException e) {
                showTemporaryError("Error updating quantity: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void updateCartTotal() {
        if (cart != null) {
            cartTotalLabel.setText(String.format("Cart Total: $%.2f", cart.getTotalPrice()));
        } else {
            cartTotalLabel.setText("Cart Total: $0.0");
        }
    }

    private void showTemporaryError(String errorMessage) {
        if (cartErrorLabel != null) {
            cartErrorLabel.setText(errorMessage);
            cartErrorLabel.setTextFill(Color.web("#a80000")); // Red for errors

            TranslateTransition shake = new TranslateTransition(Duration.millis(50), cartErrorLabel);
            shake.setByX(5);
            shake.setCycleCount(6);
            shake.setAutoReverse(true);
            shake.play();

            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> cartErrorLabel.setText(""));
            pause.play();
        }
    }

    private void showTemporarySuccess(String successMessage) {
        // This method is no longer used for checkout success
    }

    private void reselectItem(int eventId) {
        if (cart != null) {
            cartTable.getItems().stream()
                    .filter(event -> event.getEventId() == eventId)
                    .findFirst()
                    .ifPresent(event -> cartTable.getSelectionModel().select(event));
        }
    }

    private void updateTableView() {
        try {
            if (cart != null && cartTable != null) {
                System.out.println("Updating cartTable with " + cart.getItemCount() + " items");
                cartTable.setItems(FXCollections.observableArrayList(
                        cart.getItems().keySet().stream()
                                .map(eventId -> {
                                    try {
                                        Event event = DatabaseManager.getInstance().getEvent(eventId);
                                        System.out.println("Fetched event: " + event.getTitle() + " (ID: " + eventId + ")");
                                        return event;
                                    } catch (SQLException e) {
                                        System.err.println("Error fetching event ID " + eventId + ": " + e.getMessage());
                                        return null;
                                    }
                                })
                                .filter(event -> event != null)
                                .collect(Collectors.toList())
                ));
                cartTable.refresh();
                System.out.println("cartTable items count: " + cartTable.getItems().size());
            } else {
                System.out.println("cart or cartTable is null, clearing table");
                cartTable.setItems(FXCollections.observableArrayList());
                cartTable.refresh();
            }
        } catch (Exception e) {
            showTemporaryError("Error updating cart display: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCheckoutButtonState() {
        if (checkoutBtn != null) {
            boolean hasItems = cart != null && cart.getItemCount() > 0;
            checkoutBtn.setDisable(!hasItems);
            System.out.println("Checkout button state updated: " + (hasItems ? "Enabled" : "Disabled"));
        }
    }
}