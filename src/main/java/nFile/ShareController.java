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
import javafx.event.ActionEvent;
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
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		System.out.println("ShareController.initialize()");
		// TODO Auto-generated method stub
		//List<String> values = Arrays.asList("one", "two", "three");
        //listView.setItems(FXCollections.observableList(values));
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
			out.writeBytes("users="+ listViewItem.getItems().toString()+"&fileID=1");
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
				String userString = result.substring(1, result.length()-1);
				errorLabel.setText(userString + " does not exist.");
				userList.removeAll(userList);
				String[] userErrorList = userString.split(", ");
				for (int i = 0; i < userErrorList.length; i++){
					userList.add(userErrorList[i]);
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
