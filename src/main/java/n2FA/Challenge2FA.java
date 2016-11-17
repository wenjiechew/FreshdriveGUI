/**
 * Controller for the 2FAChallengeWindow JavaFX file, 
 * where all operations triggered from that window will be serviced.
 *      
 */

package n2FA;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import nObjectModel.Account;

public class Challenge2FA implements Initializable {
	@FXML
	private Button submitBtn;
	@FXML
	private TextField otpInput;
	private String result;

	private int loginAttempts = 3;

	/**
	 * This function basically gets the one time password(OTP) input by the user
	 * and sending it to the server for validation
	 * 
	 * @param event
	 *            the event being passed is the mouse event.In this case will be
	 *            the clicking of the verifiying the OTP button.Once the
	 *            verification is done and correct The system would then
	 *            redirect the user to FileObjectWindow window for the user to
	 *            start uploading and sharing files
	 */
	public void do2FASubmit(ActionEvent event) {
		try {
			URL url = new URL(nURLConstants.Constants.otpURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// Adding Header
			con.setRequestMethod("POST");

			// Send Post
			con.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.writeBytes("otp=" + otpInput.getText().toString() + "&username=" + Account.getAccount().getUsername());
			out.flush();
			out.close();

			// Response from Server
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String response;

			while ((response = in.readLine()) != null) {
				result = response;
			}
			in.close();

			// Where result is either error or the token key
			if (loginAttempts < 1) {
				makeErrorAlert("Authentication failed - No more attempts left.",
						"The OTP key you entered still did not match the system's records. Please try again after being redirected to the login page.");

				Parent loginPageParent = FXMLLoader.load(getClass().getResource("/nLogin/Login.fxml"));
				Scene loginScene = new Scene(loginPageParent);
				Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				app_stage.setScene(loginScene);
				app_stage.show();
			} else if (result.equals("expired")) {
				makeErrorAlert("One-time password expired.",
						"Your OTP has expired. You will now be redirected back to the login page.");

				Parent loginPageParent = FXMLLoader.load(getClass().getResource("/nLogin/Login.fxml"));
				Scene loginScene = new Scene(loginPageParent);
				Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				app_stage.setScene(loginScene);
				app_stage.show();
			} else if (result.equals("err")) {
				makeErrorAlert("Authentication failed - Remaining attempts: (" + loginAttempts + ")",
						"The OTP key you entered did not match the system's records. Please try again.");
				loginAttempts--;
			} else if (result.equals("token-err")) {
				makeErrorAlert("Failed to login - Remaining attempts: (" + loginAttempts + ")",
						"Something went wrong when we were logging you in. Please try again.");
				loginAttempts--;
			} else {
				// Verified to access main page now
				// Results of user information format in id-username-email-token
				// Split by divider to get each value
				String[] info = result.split("-");
				// Initialize account properly with the full account's
				// information
				Account account = Account.getAccount();
				account.set_id(info[0]);
				account.setUsername(info[1]);
				account.setEmail(info[2]);
				account.set_token(info[3]);

				Parent loginPageParent = FXMLLoader.load(getClass().getResource("/nFile/FileObjectWindow.fxml"));
				Scene loginScene = new Scene(loginPageParent);
				Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				app_stage.setScene(loginScene);
				app_stage.show();
			}
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
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
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		System.out.println("Initialize 2FA");
	}

	/**
	 * This method makes and displays an error alert based on what is being sent
	 * to the function
	 * 
	 * @param head
	 *            The string of what should be on the alert windows's head bar
	 * @param msg
	 *            The message which is supposed to be displayed in the alert
	 *            window
	 * 
	 */
	public void makeErrorAlert(String head, String msg) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("ERROR");
		alert.setHeaderText(head);
		alert.setContentText(msg);
		alert.showAndWait();
	}
}
