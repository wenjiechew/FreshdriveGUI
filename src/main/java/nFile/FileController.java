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
import java.util.Locale;
import java.util.ResourceBundle;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.JFileChooser;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
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
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nObjectModel.Account;

import com.dropbox.core.*;
import com.dropbox.core.DbxException;
import com.google.gson.Gson;
import com.jfoenix.controls.JFXTextArea;

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
	static final int BUFFER_SIZE = 524288000;
	// private static DbxClient client;
	final FileChooser fileChooser = new FileChooser();
	String[] fileIdArray;

	Account account = Account.getAccount();
	private Stage app_stage;

	// private String username = account.getUsername();
	// private String userID = account.get_id();

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

			System.out.println(result);

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
			// a real program would need to handle this exception
		} catch (IOException ex) {
			// a real program would need to handle this exception
		}
	}

	public void handleUploadFileBtn(ActionEvent event) throws IOException, DbxException {
		try {
			URL url = new URL(nURLConstants.Constants.uploadURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setUseCaches(false);
			// Adding Header
			con.setRequestMethod("POST");
			// get the filepath from inputFile
			String filePath = inputFile.getPath();
			// replace the single backslash with double backslash
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
			System.out.println("File Name: " + uploadFile.getName());

			System.out.println("File Length: " + String.valueOf(uploadFile.length()));
			System.out.println("USERNAME: " + account.getUsername());
			System.out.println("File to upload: " + filePath);
			System.out.println("File created on: " + dateFormat.format(date));
			System.out.println("Expiry date: " + expireDate);
			System.out.println("Owner ID: " + account.get_id());

			System.out.println("File Length: " + String.valueOf(uploadFile.length()));
			System.out.println("USERNAME: " + account.getUsername());

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

			System.out.println("Data was written.");
			out.close();
			inputStream.close();

			// Response from Server
			int responseCode = con.getResponseCode();
			// String error = con.getErrorStream().toString();
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
				} else if (result.equals("File uploaded")) {
					uploadedFileLabel.setText("");
					uploadFileBtn.setText("Upload");
					uploadFileBtn.setDisable(true);
					uploadBtn.setDisable(true);
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Success Dialog");
					alert.setHeaderText("Success!");
					alert.setContentText("File has been uploaded!.");
					alert.showAndWait();
				} else if(result.equals("unverified-token")){
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("ERROR");
					alert.setHeaderText("Unable to authorize user to take action.");
					alert.setContentText("The system failed to verify your identity. Please try again, or re-login if the problem persists. ");
					alert.showAndWait();
				}
				in.close();

				System.out.println(result);

				System.out.println("Server's response: " + result);

			} else {

				System.out.println("Server returned non-OK code: " + responseCode);

			}

		} catch (MalformedURLException ex) {
			// a real program would need to handle this exception
		} catch (IOException ex) {
			// a real program would need to handle this exception
		}
		initializeListView();
	}

	public void handleUploadButton(ActionEvent event)
			throws IOException, DbxException, KeyManagementException, NoSuchAlgorithmException {
		uploadedFileLabel.setText("");
		uploadFileBtn.setText("Scanning");
		uploadFileBtn.setDisable(true);
		uploadBtn.setDisable(true);
		// loadingJFXTextArea.setVisible(true);
		// opens up file dialog for user to choose
		File file = fileChooser.showOpenDialog(app_stage);

		// inputFile = file;

		// check if valid file
		// Upon successful registration, show confirmation and go back to login
		// page

		if (file != null) {
			if (file.length() <= BUFFER_SIZE) {
				try {
					// does the virus scan
					FileScan filescan = new FileScan(file);
					if(filescan.isRunningStatus())
					{
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
									} catch (KeyManagementException | JSONException | NoSuchAlgorithmException
											| IOException e) {
										// TODO Auto-generated catch block
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
											}
										});

										System.out.println("File selected: " + inputFile.getAbsolutePath());
										System.out.println("File is ok to go");

									} else {
										Platform.runLater(new Runnable() {
											@Override
											public void run() {
												uploadFileBtn.setText("Upload");
												uploadBtn.setDisable(false);
												uploadedFileLabel.setText("File is virus infected. Try another file");
											}
										});

										System.out.println("File is virus infected");

									}
								} catch (KeyManagementException | JSONException | NoSuchAlgorithmException
										| IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}).start();
					}
					else
					{
						//Show error
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("ERROR");
						alert.setHeaderText("Unable to authorize user to take action.");
						alert.setContentText("The system failed to verify your identity. Please try again, or re-login if the problem persists. ");
						alert.showAndWait();
					}



					// System.out.println(filescan.isFileInfected());

					// this result determines whether the file has a virus or not

				} catch (Exception e) {
					e.printStackTrace();
					uploadedFileLabel.setText("Invalid File. Try another file.");
					System.out.println("Invalid File");
					uploadFileBtn.setText("Upload");
					uploadBtn.setDisable(false);

				}
			} else {
				System.out.println("File too big");
				uploadFileBtn.setText("Upload");
				uploadBtn.setDisable(false);
				uploadedFileLabel.setText("File too big. Try another file.");

			}
		} else {
			uploadedFileLabel.setText("Invalid File. Try another file.");
			System.out.println("Invalid File");
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
				System.out.print("moveToShareScreen(): " + ex);
			}
		}
	}

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
				out.writeBytes("fileID=" + fileID);

				
				//Select directory to place file in
				JFileChooser chooser = new JFileChooser(); 
				String choosertitle = "Select a directory";
				   
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle(choosertitle);
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    //
			    // disable the "All files" option.
			    //
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			        System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
			        System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
			      } else {
			        System.out.println("No Selection ");
			      }
			   
				out.flush();
				out.close();
				
				String filePath = chooser.getSelectedFile().toString();
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
				//parse response string back to bytes
				String[] byteValues = result.substring(1, result.length() - 1).split(",");
				byte[] bytes = new byte[byteValues.length];

				for (int i=0, len=bytes.length; i<len; i++) {
				   bytes[i] = Byte.parseByte(byteValues[i].trim());     
				}
				
				System.out.println("URL: "  + result);
				//Create a new file to store the bytes in
				File newFile = new File(filePath+"\\" + fileName);
				newFile.setWritable(true);
				String ext1 = FilenameUtils.getExtension(newFile.getPath());
				System.out.println("EXTENSION "+ext1);
				//Set an output stream to put file in
				FileOutputStream output = new FileOutputStream(newFile);
//				
				//Write the bytes into the outputstream to create the file
				output.write(bytes);
				output.close();
				if (result.equals("Download Fail")) {
					// alert to notify download fail.
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error: Download failed");
					alert.setHeaderText(null);
					alert.setContentText("Download has failed. Try again or contact your administrator.");
					alert.showAndWait();
				} else {
					// If no fail msg then alert success
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Download success");
					alert.setHeaderText(null);
					alert.setContentText("Your file has been downloaded into your chosen folder.");
					alert.showAndWait();
				}
			} catch (Exception ex) {
				System.out.print("download(): " + ex);
			}
		}

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("FileController.initialize()");
		// TODO Auto-generated method stub

		progressBar.setVisible(false);
		uploadFileBtn.setDisable(true);


		try {
			initializeListView();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void initializeListView() throws IOException, DbxException {

		URL url = new URL(nURLConstants.Constants.retrieveURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		System.out.println("INITIALIZE LIST VIEW");
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
		// JSONArray array =(JSONArray();

		while ((response = in.readLine()) != null) {
			jsonString = response;

		}

		JSONObject jsonObj = new JSONObject(jsonString);
		JSONArray arrayJson = jsonObj.getJSONArray("fileNames");
		// System.out.println("jsonObj: " + jsonObj);
		// System.out.println("arrayJson: " + arrayJson);
		// System.out.println("get arrayjson[1]: " + arrayJson.get(0));
		// JSONObject obj = new JSONObject(arrayJson.get(1).toString());
		// System.out.println("obj : "+ obj);
		// System.out.println("obj ID: "+ obj.getString("fileId"));
		// System.out.println("obj NAME: "+ obj.getString("fileName"));
		ObservableList<String> data = FXCollections.observableArrayList();
		fileIdArray = new String[arrayJson.length()];
		for (int i = 0; i < arrayJson.length(); i++) {
			JSONObject obj = new JSONObject(arrayJson.get(i).toString());
			data.add(obj.getString("fileName"));
			fileIdArray[i] = obj.getString("fileId");
			System.out.println("file name: " + obj.getString("fileName"));
			// Do something with each error here
		}
		fileListView.setItems(data);

		// System.out.println("init list view response: "+ jsonString);
		in.close();
		// final String APP_KEY = "hlxjjkypee9pfx6";
		// final String APP_SECRET = "a9akptnjcley8jk";
		//
		// DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
		// DbxRequestConfig config = new DbxRequestConfig("FreshDrive",
		// Locale.getDefault().toString());
		//
		// // access token for the dropbox account. may need to encrypt this
		// String accessToken =
		// "-TcOHePlr9AAAAAAAAAACMWGsYvDXPTDcThy6nM8r0hwG-Mz5cEqtDxcDygkg9i3";
		//
		// client = new DbxClient(config, accessToken);
		// System.out.println("Logged on to dropbox");
		// DbxEntry.WithChildren listing =
		// client.getMetadataWithChildren("/"+username);
		// System.out.println("Files in the root path:");
		// ObservableList<String> data = FXCollections.observableArrayList();
		// for (DbxEntry child : listing.children) {
		//
		// data.add(child.name);
		// System.out.println(" " + child.name);
		// }
		// // listView.setItems(data);
		// fileListView.setItems(data);
		// System.out.println("Refresh List View");

	}
	
	public void handleRefreshBtn(ActionEvent action) throws IOException {
		try {
			initializeListView();
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
