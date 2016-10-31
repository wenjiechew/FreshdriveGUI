package nRegister;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class RegisterController implements Initializable{
	@FXML
	private TextField usernameField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private PasswordField passwordConfirmField;
	@FXML
	private TextField emailField;
	@FXML
	private Button registerBtn;
	@FXML 
	private Pane pane;
	

	public void doRegistration(ActionEvent event){
		//TODO Logic to validate registration (possibly send email verification?)
		
		//Upon successful registration, go back to login page
		try {
			Parent loginPageParent = FXMLLoader.load(getClass().getResource("/nLogin/Login.fxml"));
			Scene loginScene = new Scene(loginPageParent);
			Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			app_stage.setScene(loginScene);
			app_stage.show();
		} catch (IOException ex) {
			System.err.println("Caught IOException: " + ex.getMessage());
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
        System.out.println("RegisterController.initialize()");
        //remove focus on text fields by default
        Platform.runLater(() -> pane.requestFocus() );
	}

}
