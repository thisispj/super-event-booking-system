package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.dao.DatabaseManager;
import com.demo.supereventbookingsystem.model.Event;
import com.demo.supereventbookingsystem.model.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label availableEventsLabel;
    @FXML
    private Label selectedEventLabel;
    @FXML
    private Button editEventButton;
    @FXML
    private Button deleteEventButton;
    @FXML
    private Button addEventButton;
    @FXML
    private Button viewUserOrdersButton;
    @FXML
    private Button logoutButton;
    @FXML
    private TreeView<HBox> eventTreeView;

    private MainController mainController;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateEventTreeView();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome Admin, " + (user != null ? user.getPreferredName() : "") + "!");
        }
    }

    private void populateEventTreeView() {
        try {
            List<Event> events = DatabaseManager.getInstance().getAllEvents();
            Map<String, List<Event>> groupedEvents = events.stream()
                    .collect(Collectors.groupingBy(Event::getTitle));

            TreeItem<HBox> root = new TreeItem<>(new HBox(new Text("Events")));
            root.setExpanded(true);

            for (Map.Entry<String, List<Event>> entry : groupedEvents.entrySet()) {
                String title = entry.getKey();
                List<Event> eventList = entry.getValue();

                TreeItem<HBox> titleItem = new TreeItem<>(new HBox(new Text(title)));
                titleItem.setExpanded(true);

                for (Event event : eventList) {
                    HBox eventBox = new HBox();
                    Text eventText = new Text(event.getVenue() + " - " + event.getDay());
                    if (event.isDisabled()) {
                        eventText.setStyle("-fx-fill: #868686;"); // Gray out disabled events
                    }

                    CheckBox checkBox = getCheckBox(event, eventText);

                    eventBox.getChildren().addAll(eventText, checkBox);
                    eventBox.setSpacing(10);
                    TreeItem<HBox> eventItem = new TreeItem<>(eventBox);
                    titleItem.getChildren().add(eventItem);
                }

                root.getChildren().add(titleItem);
            }

            eventTreeView.setRoot(root);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private CheckBox getCheckBox(Event event, Text eventText) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(!event.isDisabled()); // Checked means enabled
        checkBox.setTooltip(new Tooltip("Click to enable or disable the event"));
        checkBox.setOnAction(e -> {
            boolean newStatus = !checkBox.isSelected(); // Unchecked means disabled
            try {
                DatabaseManager.getInstance().updateEventStatus(event.getEventId(), newStatus);
                event.setDisabled(newStatus);
                eventText.setStyle(newStatus ? "-fx-fill: #868686;" : "");
                eventTreeView.refresh();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        return checkBox;
    }

    @FXML
    private void handleEditEvent() {
        // To be implemented later
    }

    @FXML
    private void handleDeleteEvent() {
        // To be implemented later
    }

    @FXML
    private void handleAddEvent() {
        // To be implemented later
    }

    @FXML
    private void handleViewUserOrders() {
        // To be implemented later
    }

    @FXML
    private void handleLogout() {
        mainController.setCurrentUser(null);
        mainController.showLogin();
    }
}