package com.omar.chatapp.client;

import com.omar.chatapp.model.UserDB;
import com.omar.chatapp.model.User;
import com.omar.chatapp.util.PasswordUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class SignInController {
	

    @FXML
    private TextField username;

    @FXML
    private PasswordField userPassword;

    @FXML
    private Label labelErrorMsg;

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    void signInProcess(ActionEvent event) throws IOException {
        labelErrorMsg.setText("");
    	String username = this.username.getText();
        String password = PasswordUtil.hashPassword(userPassword.getText());
        User user = getUser(username, password);
        if (user != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("clientUI.fxml"));
            root = loader.load();
            ClientController controller2 = (ClientController) loader.getController();
            controller2.initializeCurrentUser(user);
            scene = new Scene(root);
            Stage loginStage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage = new Stage();
            stage.setTitle("Client");
            stage.setResizable(false);
            loginStage.hide();
            stage.setScene(scene);
            stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/com/omar/chatapp/Images/Chatting-icon.png")));
            stage.show();
            labelErrorMsg.setText("Login Successful!");
        }else {
            labelErrorMsg.setText("Invalid UserId or Password! Try again.");
        }
    }

    private User getUser(String username, String password) {
        if (!username.isEmpty() && !password.isEmpty()){
            return UserDB.findUser(username, password);
        }
        return null;
    }

}
