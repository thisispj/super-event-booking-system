module com.demo.supereventbookingsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;

    opens com.demo.supereventbookingsystem to javafx.fxml;
    exports com.demo.supereventbookingsystem;
    exports com.demo.supereventbookingsystem.controller;
    opens com.demo.supereventbookingsystem.controller to javafx.fxml;
}