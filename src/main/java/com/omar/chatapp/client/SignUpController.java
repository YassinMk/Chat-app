package com.omar.chatapp.client;

import com.omar.chatapp.model.UserDB;
import com.omar.chatapp.model.User;
import com.omar.chatapp.util.DatabaseUtil;
import com.omar.chatapp.util.PasswordUtil;
import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SignUpController {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Label labelMsg;
    private JFXButton btnSignInSlider;

    public void setBtnSignInSlider(JFXButton btnSignInSlider) {
        this.btnSignInSlider = btnSignInSlider;
    }

    @FXML
    void signUpProcess(ActionEvent event) {
    	if (!username.getText().isEmpty() && !password.getText().isEmpty()){
            User user = new User();
            int id = DatabaseUtil.getNumberOfRegisteredUser();
            user.setUserId(id);
            user.setUsername(username.getText());
            user.setPassword(PasswordUtil.hashPassword(password.getText()));
            labelMsg.setFont(Font.font("Arial Rounded MT Bold", FontWeight.NORMAL, 14));
            if (!UserDB.isIdenticalUser(user)){
                UserDB.addUserInFile(user);
                labelMsg.setStyle("-fx-text-fill: GREEN");
                labelMsg.setText("Registration Successful!");
                btnSignInSlider.fire();
            }else {
                labelMsg.setStyle("-fx-text-fill: #F78C7B");
                labelMsg.setText("Already Registered. Go to SignIn.");
            }
            username.clear();
            password.clear();
        }else {
            labelMsg.setStyle("-fx-text-fill: #F78C7B");
            labelMsg.setText("Registration Failed! Try again.");
        }
    }
}
