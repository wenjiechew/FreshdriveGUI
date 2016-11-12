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

public class Challenge2FA implements Initializable{
	@FXML
	private Button submitBtn;
	@FXML
	private TextField otpInput;
	private String result;
	
	private int loginAttempts=3;
	public void do2FASubmit(ActionEvent event){
		try {
			URL url = new URL(nURLConstants.Constants.otpURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
	
			// Adding Header
			con.setRequestMethod("POST");
	
			// Send Post
			con.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.writeBytes("otp=" + otpInput.getText().toString()+ "&username=" + Account.getAccount().getUsername());
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
			if(loginAttempts<1){
				makeErrorAlert("Authentication failed - No more attempts left.", "The OTP key you entered still did not match the system's records. Please try again after being redirected to the login page.");
				
				Parent loginPageParent = FXMLLoader.load(getClass().getResource("/nLogin/Login.fxml"));
				Scene loginScene = new Scene(loginPageParent);
				Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				app_stage.setScene(loginScene);
				app_stage.show();
			}
			else if(result.equals("expired")){
				makeErrorAlert("One-time password expired.", "Your OTP has expired. You will now be redirected back to the login page.");
				
				Parent loginPageParent = FXMLLoader.load(getClass().getResource("/nLogin/Login.fxml"));
				Scene loginScene = new Scene(loginPageParent);
				Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				app_stage.setScene(loginScene);
				app_stage.show();
			}
			else if(result.equals("err")) {
				makeErrorAlert("Authentication failed - Remaining attempts: ("+loginAttempts+")","The OTP key you entered did not match the system's records. Please try again.");
				loginAttempts--;
			}
			else if(result.equals("token-err")){
				makeErrorAlert("Failed to login - Remaining attempts: ("+loginAttempts+")","Something went wrong when we were logging you in. Please try again.");
				loginAttempts--;
			}
			else{
				//Verified to access main page now
				//Results of user information format in id-username-email-token
				//Split by divider to get each value
				String[] info = result.split("-");
				//Initialize account properly with the full account's information
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
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
        System.out.println("Challenge2FA.initialize()");
	}

    public void makeErrorAlert(String head, String msg){
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("ERROR");
		alert.setHeaderText(head);
		alert.setContentText(msg);
		alert.showAndWait();
    }
}
