package nLogin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import nObjectModel.Account;

public class LoginController implements Initializable {
	@FXML
	private Button loginBtn;
	@FXML
	private TextField userTextBox;
	@FXML
	private PasswordField passTextBox;
	@FXML
	private Label displayMsg;
	@FXML
	private Hyperlink registerLink;

	private String result;

	public void handleLoginBtn(ActionEvent event) {
		try {
			URL url = new URL(nURLConstants.Constants.loginURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// Adding Header
			con.setRequestMethod("POST");

			// Send Post
			con.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.writeBytes(
					"username=" + userTextBox.getText().toString() + "&password=" + passTextBox.getText().toString());
			out.flush();
			out.close();

			// Response from Server
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String response;

			while ((response = in.readLine()) != null) {
				result = response;
			}
			in.close();
			
			//Where result is either error or the token key
			System.out.println(result);

			// 1 = Failed
			if (result.contentEquals("1")) {
				displayMsg.setTextFill(Color.web("#FF0000"));
				displayMsg.setText("Login failed.");
				// Optionally display number of attempts left (if doing account lock)
			} 
			else if(result.contentEquals("active-token")){
				displayMsg.setTextFill(Color.web("#FF0000"));
				displayMsg.setText("Account already logged in.");
			}
			else {
				//Send to 2FA Page to verify emailed OTP
				Account account = Account.getAccount();
				account.setUsername(userTextBox.getText()); 
				
				Parent FilePageParent = FXMLLoader.load(getClass().getResource("/n2FA/2FAChallengeWindow.fxml"));
				Scene FilePageScene = new Scene(FilePageParent);
				Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				app_stage.setScene(FilePageScene);
				app_stage.show();
			}

		} catch (MalformedURLException ex) {
			// a real program would need to handle this exception
		} catch (IOException ex) {
			// a real program would need to handle this exception
		}
	}

	public void handleRegistration(ActionEvent event) {
		// Move to Registration page
		try {
			Parent registerPageParent = FXMLLoader.load(getClass().getResource("/nRegister/Register.fxml"));
			Scene registerScene = new Scene(registerPageParent);
			Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			app_stage.setScene(registerScene);
			app_stage.show();
		} catch (IOException ex) {
			System.err.println("Caught IOException: " + ex.getMessage());
		}
	}

	@Override // This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		System.out.println("LoginController.initialize()");
		// assert loginBtn != null : "fx:id=\"loginBtn\" was not injected: check your FXML file 'login.fxml'.";
		// initialize your logic here: all @FXML variables will have been injected
	}
}
