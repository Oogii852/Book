module com.library {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

    opens com.library to javafx.fxml;
    opens com.library.controllers to javafx.fxml;
    opens com.library.models to javafx.fxml;
    opens com.library.database to javafx.fxml;

    exports com.library;
    exports com.library.controllers;
    exports com.library.models;
    exports com.library.database;
}