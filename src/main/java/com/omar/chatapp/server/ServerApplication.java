package com.omar.chatapp.server;

import com.omar.chatapp.util.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.SQLException;

public class ServerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException, SQLException {
        System.out.println("loading server ...");
        DatabaseUtil.createUserTable();
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource("serverUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.setFill(Color.TRANSPARENT);
        stage.setTitle("Server!");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}