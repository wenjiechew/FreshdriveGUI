/**
 * Controller for the FileShareWindow JavaFX file, 
 * where all operations triggered from that window will be serviced.
 *      
 */

package nFile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import nObjectModel.Account;

public class ShareController implements Initializable {
	@FXML
	private Button addBtn;
	@FXML
	private Button removeBtn;
	@FXML
	private Button shareBtn;
	@FXML
	private Button backBtn;
	@FXML
	private Label errorLabel;
	@FXML
	private TextField userTxtField;
	@FXML
	private ListView<String> listViewItem;
	ObservableList<String> userList = FXCollections.observableArrayList();
	private int fileID;
	
	/**
	 * Sets the fileID from FileController
	 * @param fileID
	 */
	public void setFileID(int fileID){
	    this.fileID = fileID;
	}
	
	/**
	 * Initialises ShareController.
	 * A sleeping task is created to delay the program by 50 ms,
	 * It allows for the fileID to be set before continuing with subsequent tasks (i.e. getSharedUsers)
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		System.out.println("ShareController.initialize()");
		
		Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                	//Delay 50ms to ensure that fileID is set during initialisation of controller
            		errorLabel.setText("Retrieving users...");
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
        		//Get the list of users owner has shared to for this specific file
        		getSharedUsers(fileID);
        		errorLabel.setText(null);
            }
        });
        new Thread(sleeper).start();
	}
	
	/**
	 * Grant permission to users as listed in the text field.
	 * @param event
	 * @throws IOException
	 */
	@FXML
	public void addUser(ActionEvent event) throws IOException {
		if (userTxtField.getText().equals("")){
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("Please enter an username or email.");
			alert.showAndWait();
		}
		else {
			String result = null;
			try {	
				URL url = new URL(nURLConstants.Constants.sharingURL);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
	
				// Add header to request
				con.setRequestMethod("POST");
				String users = userTxtField.getText();
	
				// Post request to servlet
				con.setDoOutput(true);
				DataOutputStream out = new DataOutputStream(con.getOutputStream());
				out.writeBytes("users="+ users +"&fileID=" + fileID + "&username=" + Account.getAccount().getUsername() +  "&token=" + Account.getAccount().get_token() + "&action=add");
				out.flush();
				out.close();
	
				// Response from servlet with a list of unvalidated users (if any) and granted users (if any)
				BufferedReader in = 
	                new BufferedReader( new InputStreamReader(con.getInputStream()));			
	            String response;
	            
	            while ((response = in.readLine()) != null) {
	                result = response;
	            }
				in.close();
				
				if (result != null)
				{
					if (result.equals("File"))
					{
						errorLabel.setText("Error in uploading file, please click 'Back' and try again.");
					}
					else
					{
						//Read response in format: "[errorlist],accepted=[accepteduserlist]"
						//Split into two strings
						String[] userLists = result.split(",accepted=");
						
						//Remove brackets and print message
						String errorList = userLists[0].substring(1, userLists[0].length()-1);
						if (errorList.equals("")){
							errorLabel.setText("File successfully shared to selected users!");
						}
						else
						{
							errorLabel.setText(errorList + " does not exist.");
						}
						
						//Remove brackets and add to list
						String acceptedList = userLists[1].substring(1, userLists[1].length()-1);
						String[] acceptedUsers = acceptedList.split(",");
						for (int i = 0; i < acceptedUsers.length; i++){
							if (acceptedUsers[i].equals(""))
							{
							}
							else
							{
								userList.add(acceptedUsers[i]);
							}
						}
					}
				}
	        }
	        catch (Exception ex) {
	            System.out.print("addUser(): " + ex);
	        }
			listViewItem.setItems(userList);
			userTxtField.clear();
		}
	}

	/**
	 * Remove permission of selected user from ListView.
	 * @param event
	 * @throws IOException
	 */
	@FXML
	public void removeUser(ActionEvent event) throws IOException {
		String removedUser = listViewItem.getSelectionModel().getSelectedItem();
		
		String result = null;
		try {	
			URL url = new URL(nURLConstants.Constants.sharingURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// Add header to request
			con.setRequestMethod("POST");

			// Post request to servlet
			con.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.writeBytes("users="+ removedUser +"&fileID=" + fileID + "&username=" + Account.getAccount().getUsername() +  "&token=" + Account.getAccount().get_token() +  "&action=remove");
			out.flush();
			out.close();

			// Response from servlet
			// "File": File not found in database
			// "User": User not found or originally had no permission
			BufferedReader in = 
                new BufferedReader( new InputStreamReader(con.getInputStream()));			
            String response;
            
            while ((response = in.readLine()) != null) {
                result = response;
            }
			in.close();
			
			if (result != null)
			{
				if (result.equals("File"))
				{
					errorLabel.setText("Error in uploading file, please click 'Back' and try again.");
				}
				else if (result.equals("User"))
				{
					errorLabel.setText("Error in validating user, please try again.");
				}
				else
				{
					userList.remove(removedUser);
					errorLabel.setText(null);
				}
			}
        }
        catch (Exception ex) {
            System.out.print("removeUser(): " + ex);
        }
		listViewItem.setItems(userList);
	}
	
	/**
	 * Switch screen to file repository
	 * @param event
	 * @throws IOException
	 */
	public void returnToFileScreen(ActionEvent event) throws IOException {
		Parent FilePageParent = FXMLLoader.load(getClass().getResource("/nFile/FileObjectWindow.fxml"));
    	Scene FilePageScene = new Scene(FilePageParent);
    	Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    	app_stage.setScene(FilePageScene);
    	app_stage.show();
	}

	/**
	 * Retrieve a list of user who has access to file
	 * @param fileID
	 */
	public void getSharedUsers(int fileID){
		String result = null;
		try {	
			URL url = new URL(nURLConstants.Constants.sharingListURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// Add header to request
			con.setRequestMethod("POST");

			// Post request to servlet
			con.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.writeBytes("fileID=" + fileID);
			out.flush();
			out.close();

			// Response from servlet 
			// "File": File not found in database
			// "Unshared": Only owner has access to this specific file
			// List of shared users
			BufferedReader in = 
                new BufferedReader( new InputStreamReader(con.getInputStream()));			
            String response;
            
            while ((response = in.readLine()) != null) {
                result = response;
            }
			in.close();
			
			if (result != null)
			{
				if(result.equals("unverified-token")){
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("ERROR");
					alert.setHeaderText("Unable to authorize user to take action.");
					alert.setContentText("The system failed to verify your identity. Please try again, or re-login if the problem persists. ");
					alert.showAndWait();
				}
				else if (result.equals("File"))
				{
					errorLabel.setText("Error in finding file, please click 'Back' and try again.");
				}
				else if (result.equals("Unshared")){
					//userList is empty
				}
				else
				{
					//Receives list of users in format [user1, user2]
					//Removes bracklet and splits string into individual users
					String userString = result.substring(1, result.length()-1);
					userList.removeAll(userList);
					String[] sharedUserList = userString.split(", ");
					//Add users into a List to display on ListView
					for (int i = 0; i < sharedUserList.length; i++){
						userList.add(sharedUserList[i]);
					}
					listViewItem.setItems(userList);
				}
			}
			else {
				userList.removeAll(userList);
			}
        }
        catch (Exception ex) {
            System.out.print("getSharedUsers(): " + ex);
        }
	}
}
