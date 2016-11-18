/**
 * Controller for the FileObjectWindow JavaFX file, 
 * where all operations triggered from that window will be serviced.
 *      
 */

package nFile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import fileScan.FileScan;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
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
	@FXML
	private DatePicker expiryDatePicker;
	@FXML
	private Label uploadedFileLabel;
	@FXML
	private Button uploadFileBtn;
	@FXML
	private ListView<String> fileListView;

	private String result;
	private File inputFile;
	static final int BUFFER_SIZE = 33554432;
	final FileChooser fileChooser = new FileChooser();
	String[] fileIdArray;

	Account account = Account.getAccount();
	private Stage app_stage;

	/**
	 * Performs action for the logout button press, to send a logout request to
	 * the server.
	 *
	 * @param ActionEvent
	 *            triggered by button press
	 * @throws IOException
	 */
	public void handleLogoutBtn(ActionEvent event) throws IOException {
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
				alert.setHeaderText("Unable to logout");
				alert.setContentText("There was an error logging out.\nPlease try again!");
				alert.showAndWait();
			} else {
				// Clear Account instance
				account.clearInstance();
				// Back to login screen
				Parent FilePageParent = FXMLLoader.load(getClass().getResource("/nLogin/Login.fxml"));
				Scene FilePageScene = new Scene(FilePageParent);
				app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				app_stage.setScene(FilePageScene);
				app_stage.show();
			}

		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Performs action for the Upload button press, to send a request to the
	 * server to send and upload the user's chosen file.
	 *
	 * @param ActionEvent
	 *            triggered by button press
	 * @throws IOException
	 */
	public void handleUploadFileBtn(ActionEvent event) throws IOException {

		try {
			URL url = new URL(nURLConstants.Constants.uploadURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setUseCaches(false);
			// Adding Header
			con.setRequestMethod("POST");
			// Get the file path of the inputFile
			String filePath = inputFile.getPath();
			filePath = filePath.replace("\\", "\\\\");
			File uploadFile = new File(filePath);

			// Send Post
			// get the properties for the files information
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			Date date = new Date();
			String currentDate = dateFormat.format(date).toString();
			LocalDate expiryDate = expiryDatePicker.getValue();
			String expireDate;
			if (expiryDate != null) {
				expireDate = expiryDate.format(formatter);
			} else {
				expireDate = "";
			}

			// get the properties for the files information
			con.setRequestProperty("filePath", filePath);
			con.setRequestProperty("usertoken", account.get_token());
			con.setRequestProperty("fileName", uploadFile.getName());
			con.setRequestProperty("fileLength", String.valueOf(uploadFile.length()));
			con.setRequestProperty("username", account.getUsername());
			con.setRequestProperty("filePath", "/" + account.getUsername() + "/" + uploadFile.getName());
			con.setRequestProperty("ownerID", account.get_id());
			con.setRequestProperty("createdOn", currentDate);
			con.setRequestProperty("expiryDate", expireDate);

			// opens output stream of the HTTP connection for writing data
			OutputStream out = con.getOutputStream();

			// Opens input stream of the file for reading data
			FileInputStream inputStream = new FileInputStream(uploadFile);

			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;

			System.out.println("Start writing data...");

			// write the file into the outputstream
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			out.close();
			inputStream.close();

			// Response from Server
			int responseCode = con.getResponseCode();
			if (responseCode == con.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String response;

				while ((response = in.readLine()) != null) {
					result = response;
				}
				if (result.equals("File already exist")) {
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Warning!");
					alert.setContentText("File already exists. Please choose another file or rename your file.");

					alert.showAndWait();

				} else if (result.equals("File Uploaded")) {
					uploadedFileLabel.setText("");
					uploadFileBtn.setText("Upload");
					uploadFileBtn.setDisable(true);
					uploadBtn.setDisable(false);
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Success Dialog");
					alert.setHeaderText("Success!");
					alert.setContentText("File has been uploaded!.");
					alert.showAndWait();
				} else if (result.equals("unverified-token")) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("ERROR");
					alert.setHeaderText("Unable to authorize user to take action.");
					alert.setContentText(
							"The system failed to verify your identity. Please try again, or re-login if the problem persists. ");
					alert.showAndWait();
				}
				in.close();
			} else {
				System.out.println("Server returned non-OK code: " + responseCode);
			}

		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		initializeListView();
	}

	/**
	 * Performs action for the Choose file button press for user to select the
	 * desired file from their local directory which they intend to upload.
	 * Chosen file will be sent for a virus scan to prevent malicious files from
	 * entering the system.
	 * 
	 * @param event
	 * @throws IOException,
	 *             KeyManagementException, NoSuchAlgorithmException
	 * 
	 */
	public void handleUploadButton(ActionEvent event)
			throws IOException, KeyManagementException, NoSuchAlgorithmException {
		uploadedFileLabel.setText("");
		uploadFileBtn.setText("Scanning");
		uploadFileBtn.setDisable(true);
		uploadBtn.setDisable(true);
		progressBar.setVisible(true);

		// opens up file dialog for user to choose
		fileChooser.setTitle("Select file to upload");		
		File file = fileChooser.showOpenDialog(app_stage);

		// check if valid file
		if (file != null) {
			String ext = FilenameUtils.getExtension(file.getPath());
			// Check if file is within the size limit and the file types are
			// valid
			if (file.length() > 0 && file.length() <= BUFFER_SIZE && !ext.equalsIgnoreCase("exe") && !ext.equalsIgnoreCase("zip")
					&& !ext.equalsIgnoreCase("bin")) {
				try {
					// does the virus scan
					FileScan filescan = new FileScan(file);
					if (filescan.isRunningStatus()) {
						new Thread(new Runnable() {
							public void run() {
								while (filescan.responseStatus == 0) {
									// wait
									try {
										Thread.sleep(60000);
									} catch (InterruptedException ex) {
										Thread.currentThread().interrupt();
									}
									try {
										filescan.checkResponseStatus();
									} catch (Exception e) {
										e.printStackTrace();
									}

								}
								try {
									filescan.scanResults();
									if (!filescan.isFileInfected()) {
										inputFile = file;
										Platform.runLater(new Runnable() {
											@Override
											public void run() {
												uploadedFileLabel.setText(inputFile.getName());
												uploadFileBtn.setDisable(false);
												uploadFileBtn.setText("Upload");
												uploadBtn.setDisable(false);
												progressBar.setVisible(false);
											}
										});
									} else {
										Platform.runLater(new Runnable() {
											@Override
											public void run() {
												uploadFileBtn.setText("Upload");
												uploadBtn.setDisable(false);
												uploadedFileLabel.setText("File is virus infected. Try another file");
												progressBar.setVisible(false);
											}
										});
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}).start();
					} else {
						// Show invalid token error
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("ERROR");
						alert.setHeaderText("Unable to authorize user to take action.");
						alert.setContentText(
								"The system failed to verify your identity. Please try again, or re-login if the problem persists. ");
						alert.showAndWait();
					}

				} catch (Exception e) {
					e.printStackTrace();
					uploadedFileLabel.setText("Invalid File. Try another file.");
					uploadFileBtn.setText("Upload");
					uploadBtn.setDisable(false);
					progressBar.setVisible(false);

				}
			} else {
				uploadFileBtn.setText("Upload");
				uploadBtn.setDisable(false);
				uploadedFileLabel.setText("File is invalid. Try another file.");
				progressBar.setVisible(false);

			}
		} else {
			progressBar.setVisible(false);
			uploadedFileLabel.setText("Invalid File. Try another file.");
			uploadFileBtn.setText("Upload");
			uploadBtn.setDisable(false);
		}
	}

	/**
	 * Switches screen to sharing screen. Ensures that a file has been selected
	 * (an alert will be prompted otherwise). Checks the ownership of the file,
	 * if current user is not the owner, a notification will be shown. Moves to
	 * next screen, passing the selected file ID for to retrieve list of users
	 * who has access.
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void moveToShareScreen(ActionEvent event) throws IOException {
		int selectedFile = fileListView.getSelectionModel().getSelectedIndex();
		// Check if file is selected, else prompt alert
		if (selectedFile == -1) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText(null);
			alert.setContentText("Please select a file.");
			alert.showAndWait();
		} else {
			int fileID = Integer.parseInt(fileIdArray[selectedFile]);
			String result = null;
			try {
				URL url = new URL(nURLConstants.Constants.ownershipURL);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();

				// Add header to request
				con.setRequestMethod("POST");

				// Post request to servlet
				con.setDoOutput(true);
				DataOutputStream out = new DataOutputStream(con.getOutputStream());
				out.writeBytes("userID=" + account.get_id() + "&fileID=" + fileID);
				out.flush();
				out.close();

				// Response from servlet in boolean
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String response;

				while ((response = in.readLine()) != null) {
					result = response;
				}
				in.close();

				if (result.equals("true")) {
					// If user is the owner of the selected file, move on to
					// sharing options
					FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/nFile/FileShareWindow.fxml"));
					Parent root = (Parent) fxmlLoader.load();
					ShareController controller = fxmlLoader.<ShareController> getController();
					controller.setFileID(fileID);
					Scene scene = new Scene(root);
					Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
					app_stage.setScene(scene);
					app_stage.show();
				} else {
					// If user is not the owner of the selected file, prompt
					// alert to notify
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Warning: Permission Denied");
					alert.setHeaderText(null);
					alert.setContentText("Only file owners have the option to share files.");
					alert.showAndWait();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Performs action for the download button press, to send a request to the
	 * server to retrieve and download the user's chosen file.
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void handleDownloadBtn(ActionEvent event) throws IOException {
		int selectedFile = fileListView.getSelectionModel().getSelectedIndex();
		String fileName = fileListView.getSelectionModel().getSelectedItem();
		if (selectedFile == -1) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText(null);
			alert.setContentText("Please select a file.");
			alert.showAndWait();
		} else {
			int fileID = Integer.parseInt(fileIdArray[selectedFile]);
			try {
				URL url = new URL(nURLConstants.Constants.downloadURL);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();

				// Adding Header
				con.setRequestMethod("POST");

				// Send Post
				con.setDoOutput(true);
				DataOutputStream out = new DataOutputStream(con.getOutputStream());
				out.writeBytes("fileID=" + fileID + "&username=" + Account.getAccount().getUsername() + "&usertoken="
						+ Account.getAccount().get_token());

				// Select directory to place file in
				String choosertitle = "Select a directory";
				DirectoryChooser chooser = new DirectoryChooser();
				chooser.setInitialDirectory(new java.io.File("."));
				chooser.setTitle(choosertitle);
				//
				File selectedDirectory = chooser.showDialog(app_stage);
				out.flush();
				out.close();

				String filePath = selectedDirectory.getAbsolutePath();
				// replace the single backslash with double backslash
				filePath = filePath.replace("\\", "\\\\");

				// Response from Server
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String response;
				String result = null;
				while ((response = in.readLine()) != null) {
					result = response;
				}
				in.close();
				// parse response string back to bytes
				String[] byteValues = result.substring(1, result.length() - 1).split(",");
				byte[] bytes = new byte[byteValues.length];

				for (int i = 0, len = bytes.length; i < len; i++) {
					bytes[i] = Byte.parseByte(byteValues[i].trim());
				}

				// Create a new file to store the bytes in
				File newFile = new File(filePath + "\\" + fileName);
				newFile.setWritable(true);
				
				// Set an output stream to put file in
				FileOutputStream output = new FileOutputStream(newFile);
				
				// Write the bytes into the outputstream to create the file
				output.write(bytes);
				output.close();
				
				if (result.equals("Download Fail")) {
					// Alert to notify download fail.
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error: Download failed");
					alert.setHeaderText(null);
					alert.setContentText("Download has failed. Try again or contact your administrator.");
					alert.showAndWait();
				} else if (result.equals("unverified-token")) {
					// Alert for invalid token error
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("ERROR");
					alert.setHeaderText("Unable to authorize user to take action.");
					alert.setContentText(
							"The system failed to verify your identity. Please try again, or re-login if the problem persists. ");
					alert.showAndWait();
				} else {
					// If no fail message then alert success
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Download success");
					alert.setHeaderText(null);
					alert.setContentText("Your file has been downloaded into the specified folder.");
					alert.showAndWait();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	/**
	 * Initialize the screen and setup the listview to be displayed to user
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
		System.out.println("FileController.initialize()");
		progressBar.setVisible(false);
		uploadFileBtn.setDisable(true);
		try {
			initializeListView();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Initialize the list view which displays all available files (i.e.
	 * uploaded by or shared to the user)
	 * 
	 * @throws IOException
	 * @throws DBxException
	 */
	public void initializeListView() throws IOException {

		URL url = new URL(nURLConstants.Constants.retrieveURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		// Send Post
		con.setDoOutput(true);
		// con.setRequestProperty("username", account.getUsername());
		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		out.writeBytes("userID=" + account.get_id());
		out.flush();
		out.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String response;
		String jsonString = "";

		while ((response = in.readLine()) != null) {
			jsonString = response;
		}

		JSONObject jsonObj = new JSONObject(jsonString);
		JSONArray arrayJson = jsonObj.getJSONArray("fileNames");
		ObservableList<String> data = FXCollections.observableArrayList();
		fileIdArray = new String[arrayJson.length()];
		for (int i = 0; i < arrayJson.length(); i++) {
			JSONObject obj = new JSONObject(arrayJson.get(i).toString());
			data.add(obj.getString("fileName"));
			fileIdArray[i] = obj.getString("fileId");
		}
		fileListView.setItems(data);

		in.close();

	}

	/**
	 * Initialize the screen and setup the listview to be displayed to user
	 * 
	 * @param action
	 *            Actionevent triggered on button click
	 * @throws IOException
	 */
	public void handleRefreshBtn(ActionEvent action) throws IOException {

		try {
			initializeListView();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
