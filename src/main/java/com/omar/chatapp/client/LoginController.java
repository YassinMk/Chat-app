package com.omar.chatapp.client;

import com.jfoenix.controls.JFXButton;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable{

    @FXML
    private VBox vbox;

	@FXML
	private JFXButton btnSignIn;

    private Parent fxml;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			fxml = FXMLLoader.load(getClass().getResource("SignIn.fxml"));
			vbox.getChildren().setAll(fxml);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		TranslateTransition t = new TranslateTransition(Duration.seconds(1), vbox);
		t.setToX(vbox.getLayoutX() * 47);
		t.play();
	}
	
	@FXML
    void openSignup(ActionEvent event) {
    	TranslateTransition t = new TranslateTransition(Duration.seconds(1), vbox);
		t.setToX(10);
		t.play();
		t.setOnFinished(e->{
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("SignUp.fxml"));
				fxml = loader.load();
				vbox.getChildren().setAll(fxml);
				SignUpController signUpController = loader.getController();
				signUpController.setBtnSignInSlider(btnSignIn);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
    }
	
	@FXML
    void openSignin(ActionEvent event) {
		TranslateTransition t = new TranslateTransition(Duration.seconds(1), vbox);
		t.setToX(vbox.getLayoutX() * 47);
		t.play();
		t.setOnFinished(e->{
			try {
				fxml = FXMLLoader.load(getClass().getResource("SignIn.fxml"));
				vbox.getChildren().setAll(fxml);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
    }
	
    @FXML
    void cancelApp(ActionEvent event) {
    	Platform.exit();
    }
}
