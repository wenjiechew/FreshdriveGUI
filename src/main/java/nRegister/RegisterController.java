/**
 * Controller for the Register JavaFX file, 
 * where all operations triggered from that window will be serviced.
 *      
 */

package nRegister;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class RegisterController implements Initializable {
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
	private String result;

	/**
	 * This function first gets the input from the textboxes and validates each
	 * field.Thereafter sends the input to the server for registration which
	 * communicates with the database to input into the database
	 * 
	 * 
	 * @param ActionEvent
	 *            the event being passed is the mouse event.In this case will be
	 *            the clicking of the register button
	 * @return
	 */
	public void doRegistration(ActionEvent event) {

		// Validate that all fields are filled in
		if (usernameField.getText().trim().isEmpty() || passwordField.getText().isEmpty()
				|| passwordConfirmField.getText().isEmpty() || emailField.getText().trim().isEmpty()) {
			makeErrorAlert("Incomplete fields found", "Please provide the information for all of the boxes!");
		}

		else {
			// Validate information
			String username = usernameField.getText().trim();
			String email = emailField.getText().trim();
			String pass = passwordField.getText();
			String passCfm = passwordConfirmField.getText();

			// Email validation
			if (!isValidEmailAddress(email))
				makeErrorAlert("Invalid field", "Please provide a valid email.");
			// Check password strength
			else if (!isStrongPassword(pass)) {
				// DO NOTHING
			}
			// Password validation
			else if (!pass.equals(passCfm)) {
				makeErrorAlert("Password error", "The two passwords provided do not match.");
			} else {
				// If all good, post request to register
				try {
					URL url = new URL(nURLConstants.Constants.regURL);
					HttpURLConnection con = (HttpURLConnection) url.openConnection();

					// Adding Header
					con.setRequestMethod("POST");

					// Send Post
					con.setDoOutput(true);
					DataOutputStream out = new DataOutputStream(con.getOutputStream());
					out.writeBytes("username=" + username + "&password=" + pass + "&email=" + email);
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
						// TODO Differentiate errors (duplicate username?
						// duplicate email?)
						makeErrorAlert("Registration failed",
								"Please try again later, possibly with a different username or email address.");
					} else {
						// Upon successful registration, show confirmation and
						// go back to login page
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Successfully registered");
						alert.setHeaderText("Your registration was successful!");
						alert.setContentText("You will be directed back to the Login page."
								+ "\nPlease login with your credentials.");
						alert.showAndWait();

						Parent loginPageParent = FXMLLoader.load(getClass().getResource("/nLogin/Login.fxml"));
						Scene loginScene = new Scene(loginPageParent);
						Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
						app_stage.setScene(loginScene);
						app_stage.show();
					}
				} catch (MalformedURLException ex) {
					// a real program would need to handle this exception
				} catch (IOException ex) {
					// a real program would need to handle this exception
				}
			}
		}
	}

	/**
	 * Initialize the screen and setup the register window to be displayed to
	 * user
	 * 
	 * @param location
	 *            The location used to resolve relative paths for the root
	 *            object, or null if the location is not known.resources
	 * @param resources
	 *            The resources used to localize the root object, or null if the
	 *            root object was not localized
	 * @return
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("RegisterController.initialize()");
		// remove focus on text fields by default
		Platform.runLater(() -> pane.requestFocus());
	}

	/**
	 * Validate the email address entered by the user
	 * 
	 * @param email
	 *            The string variable of email being sent in to validate
	 * @return boolean Returns true when the string sent is a valid
	 *         email,returns false if it doesnt match the pattern required for
	 *         an email
	 */
	public boolean isValidEmailAddress(String email) {
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		Pattern p = java.util.regex.Pattern.compile(ePattern);
		Matcher m = p.matcher(email);
		return m.matches();
	}

	/**
	 * Validate the desired password entered by the user
	 * 
	 * @param password
	 *            The string variable of password being sent in to validate
	 * @return boolean Returns true when the string sent is a strong password
	 *         ,returns false if it doesnt match the pattern required for a
	 *         strong password
	 */
	public boolean isStrongPassword(String password) {
		boolean bool = true;
		List<String> errors = new ArrayList<String>();
		Pattern specCharPattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
		Pattern UpperCasePattern = Pattern.compile("[A-Z ]");
		Pattern lowerCasePattern = Pattern.compile("[a-z ]");
		Pattern digitCasePattern = Pattern.compile("[0-9 ]");

		if (password.length() < 8) {
			errors.add("- Your password length must be at least 8 characters.\n");
		}
		if (!specCharPattern.matcher(password).find()) {
			errors.add("- Include at least one special character in your password.\n");
		}
		if (!UpperCasePattern.matcher(password).find()) {
			errors.add("- Include at least one uppercase alphabet in your password.\n");
		}
		if (!lowerCasePattern.matcher(password).find()) {
			errors.add("- Include at least one lowercase alphabet in your password.\n");
		}
		if (!digitCasePattern.matcher(password).find()) {
			errors.add("- Include at least one number in your password.");
		}
		if (errors.size() > 0) {
			bool = false;
			// Build error string
			String errorlist = "";
			for (String e : errors) {
				errorlist = errorlist + e;
			}
			makeErrorAlert("Password too weak!", errorlist);
		}
		return bool;
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
