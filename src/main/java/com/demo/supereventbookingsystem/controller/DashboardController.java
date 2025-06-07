package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.dao.DatabaseManager;
import com.demo.supereventbookingsystem.model.Cart;
import com.demo.supereventbookingsystem.model.Event;
import com.demo.supereventbookingsystem.model.User;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label availableEventsLabel;
    @FXML
    private TableView<Event> eventTable;
    @FXML
    private TableColumn<Event, Integer> eventIdColumn;
    @FXML
    private TableColumn<Event, String> titleColumn;
    @FXML
    private TableColumn<Event, String> venueColumn;
    @FXML
    private TableColumn<Event, String> dayColumn;
    @FXML
    private TableColumn<Event, Double> priceColumn;
    @FXML
    private TableColumn<Event, Integer> availableColumn;
    @FXML
    private TextField quantityField;
    @FXML
    private Button addToCartButton;
    @FXML
    private Button viewCartButton;

    private MainController mainController;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up column value factories
        eventIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getEventId()).asObject());
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        venueColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getVenue()));
        dayColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDay()));
        priceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        availableColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTotalTickets() - cellData.getValue().getSoldTickets()).asObject());

        // Load events
        loadEvents();

        // Initial button and field disablement
        addToCartButton.setDisable(true);
        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            addToCartButton.setDisable(newSelection == null || quantityField.getText().isEmpty());
        });

        quantityField.textProperty().addListener((obs, oldValue, newValue) -> {
            addToCartButton.setDisable(newValue.isEmpty() || eventTable.getSelectionModel().getSelectedItem() == null);
        });
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getPreferredName() + "!");
        if (statusLabel != null) {
            updateCartCount();
        }
        loadEvents();
    }

    private void loadEvents() {
        try {
            System.out.println("Loading events for TableView...");
            List<Event> allEvents = DatabaseManager.getInstance().getAllEvents();
            // Filter out disabled events
            ObservableList<Event> events = FXCollections.observableArrayList(allEvents.stream()
                    .filter(event -> !event.isDisabled())
                    .collect(Collectors.toList()));
            System.out.println("Loaded " + events.size() + " enabled events.");

            if (eventTable == null || eventIdColumn == null || titleColumn == null || venueColumn == null ||
                    dayColumn == null || priceColumn == null || availableColumn == null) {
                System.err.println("FXML binding error: One or more UI components are null!");
                showTemporaryError("Error: TableView not initialized properly.");
                return;
            }

            // Set cell factories
            setColumnTextFactory(eventIdColumn, event -> String.valueOf(event.getEventId()));
            setColumnTextFactory(titleColumn, Event::getTitle);
            setColumnTextFactory(venueColumn, Event::getVenue);
            setColumnTextFactory(dayColumn, Event::getDay);
            setColumnTextFactory(priceColumn, event -> String.format("%.2f", event.getPrice()));

            availableColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        Event event = getTableRow().getItem();
                        int available = event.getAvailableTickets();
                        setText(available == 0 ? "Sold Out" : String.valueOf(available));
                        setTextFill(available == 0 ? Color.web("#F08080") : Color.BLACK);
                        System.out.println("Available cell: " + (available == 0 ? "Sold Out" : available));
                    }
                }
            });

            eventTable.setItems(events);
            eventTable.refresh();
            availableEventsLabel.setText(events.isEmpty() ? "No events available." : "Available Events: " + events.size());

            // Toggle cart controls based on selection
            eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                boolean disable = (newSel == null || newSel.getAvailableTickets() <= 0);
                addToCartButton.setDisable(disable);
                quantityField.setDisable(disable);
            });

            // Optional: log loaded events
            events.forEach(event -> System.out.printf("Event: %s, Venue: %s, Day: %s, Price: %.2f, Available: %d%n",
                    event.getTitle(), event.getVenue(), event.getDay(), event.getPrice(), event.getAvailableTickets()));

        } catch (SQLException e) {
            showTemporaryError("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showTemporaryError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private <T> void setColumnTextFactory(TableColumn<Event, T> column, Function<Event, String> textProvider) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Event event = getTableRow().getItem();
                    setText(textProvider.apply(event));
                    setTextFill(Color.BLACK);
                }
            }
        });
    }

    @FXML
    private void handleAddToCart(ActionEvent event) {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showTemporaryError("Please select an event.");
            return;
        }

        String quantityText = quantityField.getText();
        if (quantityText.isEmpty()) {
            showTemporaryError("Please enter a quantity.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showTemporaryError("Quantity must be greater than 0.");
                return;
            }

            Cart cart = DatabaseManager.getInstance().getCartItems(currentUser.getUsername());
            boolean eventExists = cart.getItems().containsKey(selectedEvent.getEventId());
            int existingQuantity = eventExists ? cart.getItems().get(selectedEvent.getEventId()) : 0;
            int totalRequestedQuantity = existingQuantity + quantity;

            if (totalRequestedQuantity > selectedEvent.getAvailableTickets()) {
                showTemporaryError("Not enough tickets available. Requested: " + totalRequestedQuantity + ", Available: " + selectedEvent.getAvailableTickets());
                return;
            }

            if (eventExists) {
                updateCartItemQuantity(currentUser.getUsername(), selectedEvent.getEventId(), totalRequestedQuantity);
            } else {
                DatabaseManager.getInstance().addToCart(currentUser.getUsername(), selectedEvent, quantity);
            }

            quantityField.clear();
            updateCartCount();
        } catch (NumberFormatException e) {
            showTemporaryError("Please enter a valid number.");
        } catch (SQLException e) {
            showTemporaryError("Error: " + e.getMessage());
            System.err.println("SQLException in handleAddToCart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewCart(ActionEvent event) {
        try {
            Cart cart = DatabaseManager.getInstance().getCartItems(currentUser.getUsername());
            mainController.showCart(cart, currentUser.getUsername());
        } catch (SQLException e) {
            showTemporaryError("Database error: " + e.getMessage());
            System.err.println("SQLException in handleViewCart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewOrders(ActionEvent event) {
        mainController.showOrders(currentUser.getUsername());
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        mainController.setCurrentUser(null);
        mainController.showLogin();
    }

    private void updateCartCount() {
        if (currentUser != null && statusLabel != null) {
            try {
                Cart cart = DatabaseManager.getInstance().getCartItems(currentUser.getUsername());
                int itemCount = cart.getItemCount();
                statusLabel.setText("Items in Cart: " + itemCount);
                if (viewCartButton != null) {
                    viewCartButton.setDisable(itemCount == 0);
                }
            } catch (SQLException e) {
                statusLabel.setText("Items in Cart: 0");
                if (viewCartButton != null) {
                    viewCartButton.setDisable(true);
                }
                System.err.println("Error updating cart count: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showTemporaryError(String errorMessage) {
        if (statusLabel != null) {
            statusLabel.setText(errorMessage);
            statusLabel.setTextFill(Color.web("#a80000"));

            TranslateTransition shake = new TranslateTransition(Duration.millis(50), statusLabel);
            shake.setByX(5);
            shake.setCycleCount(6);
            shake.setAutoReverse(true);
            shake.play();

            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> {
                updateCartCount();
                statusLabel.setTextFill(Color.web("#000000"));
            });
            pause.play();
        }
    }

    private void updateCartItemQuantity(String username, int eventId, int newQuantity) throws SQLException {
        String sql = "UPDATE cart SET quantity = ?, total_price = ? WHERE username = ? AND event_id = ?";
        try (var pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            Event event = DatabaseManager.getInstance().getEvent(eventId);
            if (event != null) {
                pstmt.setInt(1, newQuantity);
                pstmt.setDouble(2, event.getPrice() * newQuantity);
                pstmt.setString(3, username);
                pstmt.setInt(4, eventId);
                pstmt.executeUpdate();
            }
        }
    }
}