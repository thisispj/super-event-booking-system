package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.dao.DatabaseManager;
import com.demo.supereventbookingsystem.model.Cart;
import com.demo.supereventbookingsystem.model.Event;
import com.demo.supereventbookingsystem.model.User;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

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

    private MainController mainController;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("DashboardController initialized. FXML location: " + (location != null ? location.toString() : "null"));
        if (eventTable == null) {
            System.err.println("eventTable is null! FXML binding failed.");
        }
        try {
            DatabaseManager.getInstance().loadEventsFromFile("events.dat");
        } catch (SQLException e) {
            System.err.println("Error loading events from file: " + e.getMessage());
            e.printStackTrace();
        }
        if (currentUser != null) {
            loadEvents();
            updateCartCount();
        }
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
            ObservableList<Event> events = FXCollections.observableArrayList(allEvents);
            System.out.println("Loaded " + events.size() + " events.");

            if (eventTable == null || eventIdColumn == null || titleColumn == null || venueColumn == null || dayColumn == null || priceColumn == null || availableColumn == null) {
                System.err.println("FXML binding error: One or more UI components are null!");
                showTemporaryError("Error: TableView not initialized properly.");
                return;
            }

            eventIdColumn.setCellFactory(column -> new TableCell<Event, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        Event event = (Event) getTableRow().getItem();
                        setText(String.valueOf(event.getEventId()));
                        setTextFill(Color.BLACK);
                        System.out.println("Event ID cell: " + event.getEventId());
                    }
                }
            });

            titleColumn.setCellFactory(column -> new TableCell<Event, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        Event event = (Event) getTableRow().getItem();
                        setText(event.getTitle());
                        setTextFill(Color.BLACK);
                        System.out.println("Title cell: " + event.getTitle());
                    }
                }
            });

            venueColumn.setCellFactory(column -> new TableCell<Event, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        Event event = (Event) getTableRow().getItem();
                        setText(event.getVenue());
                        setTextFill(Color.BLACK);
                        System.out.println("Venue cell: " + event.getVenue());
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
                        Event event = (Event) getTableRow().getItem();
                        setText(event.getDay());
                        setTextFill(Color.BLACK);
                        System.out.println("Day cell: " + event.getDay());
                    }
                }
            });

            priceColumn.setCellFactory(column -> new TableCell<Event, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        Event event = (Event) getTableRow().getItem();
                        setText(String.format("%.2f", event.getPrice()));
                        setTextFill(Color.BLACK);
                        System.out.println("Price cell: " + event.getPrice());
                    }
                }
            });

            availableColumn.setCellFactory(column -> new TableCell<Event, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        Event event = (Event) getTableRow().getItem();
                        setText(String.valueOf(event.getAvailableTickets()));
                        setTextFill(Color.BLACK);
                        System.out.println("Available cell: " + event.getAvailableTickets());
                    }
                }
            });

            eventTable.setItems(events);
            eventTable.refresh();
            System.out.println("TableView populated with " + eventTable.getItems().size() + " items.");

            if (events.isEmpty()) {
                availableEventsLabel.setText("No events available.");
            } else {
                availableEventsLabel.setText("Available Events: " + events.size());
            }

            events.forEach(event -> System.out.println("Event: " + event.getTitle() + ", Venue: " + event.getVenue() + ", Day: " + event.getDay() + ", Price: " + event.getPrice() + ", Available: " + event.getAvailableTickets()));
        } catch (SQLException e) {
            showTemporaryError("Database error: " + e.getMessage());
            System.err.println("SQLException in loadEvents: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showTemporaryError("Database error: " + e.getMessage());
            System.err.println("Unexpected error in loadEvents: " + e.getMessage());
            e.printStackTrace();
        }
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
            if (quantity > selectedEvent.getAvailableTickets()) {
                showTemporaryError("Not enough tickets available.");
                return;
            }

            DatabaseManager.getInstance().addToCart(currentUser.getUsername(), selectedEvent, quantity);
            quantityField.clear();
            updateCartCount();
        } catch (NumberFormatException e) {
            showTemporaryError("Please enter a valid number.");
        } catch (SQLException e) {
            showTemporaryError("Database error: " + e.getMessage());
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
        // To be implemented
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
                statusLabel.setText("Items in Cart: " + cart.getItems().size());
            } catch (SQLException e) {
                statusLabel.setText("Items in Cart: 0");
                System.err.println("Error updating cart count: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showTemporaryError(String errorMessage) {
        if (statusLabel != null) {
            // Set error message and color
            statusLabel.setText(errorMessage);
            statusLabel.setTextFill(Color.web("#a80000")); // Set to red

            // Create shaking effect using TranslateTransition
            TranslateTransition shake = new TranslateTransition(Duration.millis(50), statusLabel);
            shake.setByX(5); // Move 5 pixels to the right
            shake.setCycleCount(6); // Repeat 6 times (3 full shakes)
            shake.setAutoReverse(true); // Reverse direction each cycle

            // Play the shaking effect
            shake.play();

            // Set up the 3-second timer to revert text and color
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> {
                updateCartCount(); // Revert text to cart count
                statusLabel.setTextFill(Color.web("#000000")); // Revert to black
            });
            pause.play();
        }
    }
}