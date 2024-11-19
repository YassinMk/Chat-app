module com.omar.chatapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jfoenix;

    requires org.controlsfx.controls;
    requires java.persistence;
    requires java.sql;

    exports com.omar.chatapp.server;
    opens com.omar.chatapp.server to javafx.fxml;
    exports com.omar.chatapp.client;
    opens com.omar.chatapp.client to javafx.fxml;
}