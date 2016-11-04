package nFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONException;

import fileScan.FileScan;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

	final FileChooser fileChooser = new FileChooser();
	private Stage app_stage;

	public void handleLogoutBtn(ActionEvent event) throws IOException {

		// TODO: Proper Logging out of Account, Removing of Token

		Parent FilePageParent = FXMLLoader.load(getClass().getResource("/nLogin/Login.fxml"));
		Scene FilePageScene = new Scene(FilePageParent);
		app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		app_stage.setScene(FilePageScene);
		app_stage.show();
	}

	public void handleUploadButton(ActionEvent event) throws IOException {

		// opens up file dialog for user to choose
		File file = fileChooser.showOpenDialog(app_stage);

		// check if valid file
		if (file != null) {
			try {
				// does the virus scan
				FileScan filescan = new FileScan(file);
				// this result determines whether the file has a virus or not
				if (!filescan.isFileInfected()) {
					// TODO: if file is not infected do necessary
				} else {
					System.out.println("File is virus infected");
				}
			} catch (Exception e) {
				System.out.println("Invalid File");
			}
		} else {
			System.out.println("Invalid File");
		}

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("FileController.initialize()");
		// TODO Auto-generated method stub
		progressBar.setVisible(false);
	}

}
