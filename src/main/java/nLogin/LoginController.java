/**
 * Controller for the Login JavaFX file, 
 * where all operations triggered from that window will be serviced.
 *      
 */

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

	/**
	 * This function basically handles what happens when the user tries to log
	 * in. It will first get the user name from the textbox and the password.
	 * The credentials would then be sent to the server for validation to check
	 * if the credentials match the one in the database and send an email to
	 * verified email for the one time password. The GUI would then change to
	 * the challengewindow.fxml,to await the user's input of OTP sent to the
	 * email
	 * 
	 * @param ActionEvent
	 *            the event being passed is the mouse event.In this case will be
	 *            the clicking of the register button
	 * @return
	 */
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

			// 1 = Failed
			if (result.contentEquals("1")) {
				displayMsg.setTextFill(Color.web("#FF0000"));
				displayMsg.setText("Login failed.");
				// Optionally display number of attempts left (if doing account
				// lock)
			} else if (result.contentEquals("active-token")) {
				displayMsg.setTextFill(Color.web("#FF0000"));
				displayMsg.setText("Account already logged in.");
			} else {
				// Send to 2FA Page to verify emailed OTP
				Account account = Account.getAccount();
				account.setUsername(userTextBox.getText());

				Parent FilePageParent = FXMLLoader.load(getClass().getResource("/n2FA/2FAChallengeWindow.fxml"));
				Scene FilePageScene = new Scene(FilePageParent);
				Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				app_stage.setScene(FilePageScene);
				app_stage.show();
			}

		} catch (MalformedURLException ex) {
			displayMsg.setTextFill(Color.web("#FF0000"));
			displayMsg.setText("Login URL error.");
		} catch (IOException ex) {
			ex.printStackTrace();
			displayMsg.setTextFill(Color.web("#FF0000"));
			displayMsg.setText("Login IO error.");
		} catch (Exception ex) {
			displayMsg.setTextFill(Color.web("#FF0000"));
			displayMsg.setText("Login error.");
		}
	}

	/**
	 * This function basically handles what happens when a new user tries to
	 * register. The GUI would then change to the Register.fxml,to await the
	 * user's input for the registration details sent to the email
	 * 
	 * @param event
	 *            the event being passed is the mouse event.In this case will be
	 *            the clicking of the mouse
	 * @return
	 */
	public void handleRegistration(ActionEvent event) {
		// Move to Registration page
		try {
			Parent registerPageParent = FXMLLoader.load(getClass().getResource("/nRegister/Register.fxml"));
			Scene registerScene = new Scene(registerPageParent);
			Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			app_stage.setScene(registerScene);
			app_stage.show();
		} catch (IOException ex) {
			displayMsg.setTextFill(Color.web("#FF0000"));
			displayMsg.setText("Registration page IO error.");
		} catch (Exception ex) {
			displayMsg.setTextFill(Color.web("#FF0000"));
			displayMsg.setText("Registration page error.");
		}
	}

	/**
	 * Initialize the screen and setup the login window to be displayed to user
	 * 
	 * @param location
	 *            The location used to resolve relative paths for the root
	 *            object, or null if the location is not known.resources
	 * @param resources
	 *            The resources used to localize the root object, or null if the
	 *            root object was not localized.
	 */
	@Override
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		//Intialize login
	}
}
