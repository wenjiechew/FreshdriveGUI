package nFile;

import java.io.IOException;
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
	
	public void handleLogoutBtn(ActionEvent event) throws IOException{
		
		//TO-DO Proper Logging out of Account, Removing of Token
		
		
		
		
		Parent FilePageParent = FXMLLoader.load( getClass().getResource("/nLogin/Login.fxml") );
    	Scene FilePageScene = new Scene(FilePageParent);
    	Stage app_stage = (Stage) ( (Node) event.getSource() ).getScene().getWindow();
    	app_stage.setScene(FilePageScene);
    	app_stage.show();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("FileController.initialize()");
		// TODO Auto-generated method stub
		progressBar.setVisible(false);
	} 

	
}
