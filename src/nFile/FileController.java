package nFile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import fileScan.FileScan;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nObjectModel.Account;

public class FileController implements Initializable {
	@FXML
	private Button logoutBtn;
	@FXML
	private Button uploadBtn;
	@FXML
	private Button shareBtn;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private ListView<Account> listView;
	private String result;
	final FileChooser fileChooser = new FileChooser();
	
	Account account = Account.getAccount();
	
	public void handleLogoutBtn(ActionEvent event) throws IOException {
		try{
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
		
		System.out.println(result);

		// 1 = Failed
		if (result.contentEquals("1")) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("ERROR");
			alert.setHeaderText("Unable to logout");
			alert.setContentText("There was an error logging out.\nPlease try again!");
			alert.showAndWait();
		}
		else {
			//Clear Account instance
			account.clearInstance();
			//Back to login screen
			Parent FilePageParent = FXMLLoader.load(getClass().getResource("/nLogin/Login.fxml"));
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


	public void handleUploadButton(ActionEvent event) throws IOException {

//		// TODO:find out what's the stage here.is it primarystage or what
//		File file = fileChooser.showOpenDialog(stage);
//		
//		//once file is chosen,put in the filescan parameter
//		if (file != null) {
//			FileScan filescan = new FileScan(file);
//			//this result determines whether the file has a virus or not
//			System.out.println(filescan.isFileInfected());
//		}

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("FileController.initialize()");
		// TODO Auto-generated method stub
		progressBar.setVisible(false);
	}

}
