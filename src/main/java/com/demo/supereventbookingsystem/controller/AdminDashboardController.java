package com.demo.supereventbookingsystem.controller;

import com.demo.supereventbookingsystem.dao.DatabaseManager;
import com.demo.supereventbookingsystem.model.Event;
import com.demo.supereventbookingsystem.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
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
    private Event selectedEvent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        editEventButton.setDisable(true);
        deleteEventButton.setDisable(true);
        populateEventTreeView();
        eventTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedEvent = (newVal != null && !newVal.isLeaf()) ? null : getSelectedEvent(newVal);
            editEventButton.setDisable(selectedEvent == null);
            deleteEventButton.setDisable(selectedEvent == null);
            selectedEventLabel.setText(selectedEvent != null ? selectedEvent.toString() : "None");
        });
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome " + (user != null ? user.getPreferredName() : "") + "!");
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
                    eventBox.setUserData(event); // Store the Event in the HBox
                    Text eventText = new Text(event.getVenue() + " - " + event.getDay());
                    if (event.isDisabled()) {
                        eventText.setStyle("-fx-fill: #868686;"); // Gray out disabled events
                    }

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

    public void refreshEventTreeView() {
        populateEventTreeView();
    }

    private Event getSelectedEvent(TreeItem<HBox> item) {
        if (item != null && !item.isLeaf() && item.getValue() != null) {
            return null; // Return null for parent nodes (titles)
        }
        if (item != null && item.getValue() != null && item.getValue().getUserData() instanceof Event) {
            return (Event) item.getValue().getUserData(); // Retrieve the Event from the HBox
        }
        return null;
    }

    @FXML
    private void handleEditEvent() {
        if (selectedEvent == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/demo/supereventbookingsystem/view/editevent.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            EditEventController editEventController;
            if (controller instanceof EditEventController) {
                editEventController = (EditEventController) controller;
            } else {
                throw new IllegalStateException("Loaded controller is not an instance of EditEventController: " + controller.getClass().getName());
            }
            editEventController.setMainController(mainController);
            editEventController.setCurrentUser(currentUser);
            editEventController.setSelectedEvent(selectedEvent);

            Stage editEventStage = new Stage();
            editEventStage.initModality(Modality.WINDOW_MODAL);
            editEventStage.initOwner(eventTreeView.getScene().getWindow());
            editEventStage.setScene(new Scene(root));
            editEventStage.setTitle("Edit Event");
            editEventStage.showAndWait();
            // Repopulate TreeView after modal closes
            refreshEventTreeView();
        } catch (IOException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Could not load Edit Event scene.");
            errorAlert.setContentText("An error occurred: " + e.getMessage());
            errorAlert.showAndWait();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Controller Error");
            errorAlert.setHeaderText("Incompatible controller loaded for Edit Event scene.");
            errorAlert.setContentText("An error occurred: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    @FXML
    private void handleDeleteEvent() {
        if (selectedEvent != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Are you sure you want to delete the Event: {" + selectedEvent.getEventId() + ": " + selectedEvent.getTitle() + " at " + selectedEvent.getVenue() + " on " + selectedEvent.getDay() + "}");
            alert.setContentText("This will soft delete the event. Click 'Delete' to proceed or 'Cancel' to abort.");

            ButtonType deleteButton = new ButtonType("Delete");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(deleteButton, cancelButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == deleteButton) {
                    try {
                        DatabaseManager.getInstance().softDeleteEvent(selectedEvent.getEventId());
                        populateEventTreeView(); // Repopulate to reflect the change
                        selectedEvent = null;
                        editEventButton.setDisable(true);
                        deleteEventButton.setDisable(true);
                        selectedEventLabel.setText("None");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Deletion Failed");
                        errorAlert.setHeaderText("Could not delete the event.");
                        errorAlert.setContentText("An error occurred: " + e.getMessage());
                        errorAlert.showAndWait();
                    }
                }
            });
        }
    }

    @FXML
    private void handleAddEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/demo/supereventbookingsystem/view/addevent.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            AddEventController addEventController;
            if (controller instanceof AddEventController) {
                addEventController = (AddEventController) controller;
            } else {
                throw new IllegalStateException("Loaded controller is not an instance of AddEventController: " + controller.getClass().getName());
            }
            addEventController.setMainController(mainController);
            addEventController.setCurrentUser(currentUser);

            Stage addEventStage = new Stage();
            addEventStage.initModality(Modality.WINDOW_MODAL);
            addEventStage.initOwner(eventTreeView.getScene().getWindow());
            addEventStage.setScene(new Scene(root));
            addEventStage.setTitle("Add Event");
            addEventStage.showAndWait();
            // Repopulate TreeView after modal closes
            refreshEventTreeView();
        } catch (IOException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Could not load Add Event scene.");
            errorAlert.setContentText("An error occurred: " + e.getMessage());
            errorAlert.showAndWait();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Controller Error");
            errorAlert.setHeaderText("Incompatible controller loaded for Add Event scene.");
            errorAlert.setContentText("An error occurred: " + e.getMessage());
            errorAlert.showAndWait();
        }
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