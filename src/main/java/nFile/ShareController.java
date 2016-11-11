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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
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
	private String fileName;
	private int fileOwnerID;
	
	public void setFile(String fileName){
	    this.fileName = fileName;
	}
	
	public void setFileOwnerID(int userID){
	    this.fileOwnerID = userID;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		System.out.println("ShareController.initialize()");
		// TODO Auto-generated method stub
		
		Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
            		errorLabel.setText("Retrieving users...");
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
        		//Get the list of users owner has shared to for this specific file
        		getSharedUsers(fileOwnerID, fileName);
        		errorLabel.setText("");
            }
        });
        new Thread(sleeper).start();
	}
	
	@FXML
	public void addUser(ActionEvent event) throws IOException {
		userList.add(userTxtField.getText());
		listViewItem.setItems(userList);
		userTxtField.clear();
		System.out.println(listViewItem.getItems());
	}

	@FXML
	public void removeUser(ActionEvent event) throws IOException {
		userList.removeAll(listViewItem.getSelectionModel().getSelectedItems()); 
	}
	
	public void returnToFileScreen(ActionEvent event) throws IOException {
		Parent FilePageParent = FXMLLoader.load(getClass().getResource("/nFile/FileObjectWindow.fxml"));
    	Scene FilePageScene = new Scene(FilePageParent);
    	Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    	app_stage.setScene(FilePageScene);
    	app_stage.show();
	}

	public void getSharedUsers(int fileOwnerID, String fileName){
		String result = null;
		try {	
			URL url = new URL(nURLConstants.Constants.sharingListURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			//Adding Header
			con.setRequestMethod("POST");
			
			//Send Post
			con.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.writeBytes("ownerID=" + fileOwnerID + "&filename=" + fileName);
			out.flush();
			out.close();
			
			//Response from Server
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
					errorLabel.setText("Error in finding file, please click 'Back' and try again.");
				}
				else if (result.equals("Unshared")){
					//userList is empty
				}
				else
				{
					String userString = result.substring(1, result.length()-1);
					userList.removeAll(userList);
					String[] sharedUserList = userString.split(", ");
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
            System.out.print("handleSharing(): " + ex);
        }
	}
	
	public void handleSharing(ActionEvent event){		
		String result = null;
		try {	
			URL url = new URL(nURLConstants.Constants.sharingURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			//Adding Header
			con.setRequestMethod("POST");
			
			//Send Post
			con.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.writeBytes("users="+ listViewItem.getItems().toString()+"&ownerID=" + fileOwnerID + "&filename=" + fileName);
			out.flush();
			out.close();
			
			//Response from Server
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
					String userString = result.substring(1, result.length()-1);
					errorLabel.setText(userString + " does not exist.");
					userList.removeAll(userList);
					String[] userErrorList = userString.split(", ");
					for (int i = 0; i < userErrorList.length; i++){
						userList.add(userErrorList[i]);
					}
				}
			}
			else {
				userList.removeAll(userList);
			}
        }
        catch (Exception ex) {
            System.out.print("handleSharing(): " + ex);
        }
	}
}
