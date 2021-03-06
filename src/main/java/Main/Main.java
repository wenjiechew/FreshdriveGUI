/**
 * The main controller for the javafx project
 *
 *      
 */

package Main;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import nObjectModel.Account;
import nURLConstants.Constants;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Main extends Application {

	/**
	 * start method to set the first screen the user will see when he runs the
	 * application.And also setting up the server host name
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/nLogin/Login.fxml"));
		primaryStage.setTitle("FreshDrive");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	public static void main(String[] args) {
		System.setProperty("javax.net.ssl.trustStore", ".keystore");
//		System.setProperty("javax.net.debug", "ssl");
		launch(args);
	}

	static {
		// for localhost testing only(needed if server is running locally)
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

			public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
				if (hostname.equals( Constants.hostname )) {
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * The application closes and auto log outs the user when the close button
	 * on the top right hand corner is clicked
	 */
	@Override
	public void stop() {
		System.out.println("Closing application.");
		// If logged in, do logout to remove token
		Account account = Account.getAccount();
		String result = null;
		if (account.getUsername() != null && account.get_token() != null) {
			System.out.println("Doing auto-logout");
			try {
				URL url = new URL(nURLConstants.Constants.logoutURL);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();

				// Adding Header
				con.setRequestMethod("POST");

				// Send Post
				con.setDoOutput(true);
				DataOutputStream out = new DataOutputStream(con.getOutputStream());
				out.writeBytes("username=" + account.getUsername());
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
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("ERROR");
					alert.setHeaderText("Unable to close application.");
					alert.setContentText("There was an error logging out of your account.\nPlease try again!");
					alert.showAndWait();
				} else {
					// Clear Account instance
					account.clearInstance();
				}
			} catch (MalformedURLException ex) {
				makeErrorAlert("Operation failed", "Oops, a logout URL error has occurred. Try again, or report to admin if problem persists");
			} catch (Exception ex) {
				makeErrorAlert("Operation failed", "Oops, a logout error has occurred. Try again, or report to admin if problem persists");
			}
		}
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
