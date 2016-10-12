package nLogin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;

import javax.net.ssl.HttpsURLConnection;

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



public class LoginController implements Initializable{
	@FXML
	private Button loginBtn;
	@FXML
	private TextField userTextBox;
	@FXML
	private PasswordField passTextBox;
	@FXML
	private Label displayMsg;
	
	

	public void handleLoginBtn(ActionEvent event){
		try {	
			URL url = new URL(nURLConstants.Constants.loginURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			//Adding Header
			con.setRequestMethod("POST");
			
			//Send Post
			con.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(	con.getOutputStream()	);
			out.writeBytes(	"username="+ userTextBox.getText().toString() + "&password=" + passTextBox.getText().toString() );
			out.flush();
			out.close();
						
			
			//Response from Server
			BufferedReader in = 
                new BufferedReader( new InputStreamReader(	con.getInputStream() ) );
            
            String response;
            while ( (response = in.readLine()) != null ) {
                displayMsg.setText(response);
            }
            in.close();
            
            
            
            //Check for PROPER RESULTS .. tO EDITED!! 
            
            // 1 = Failed
            
            if(displayMsg.getText().toString().contentEquals("U're Validated") ){
//            	Parent FilePageParent = FXMLLoader.load( getClass().getResource("/nFile/FileObjectWindow.fxml") );
//            	Scene FilePageScene = new Scene(FilePageParent);
//            	Stage app_stage = (Stage) ( (Node) event.getSource() ).getScene().getWindow();
//            	app_stage.setScene(FilePageScene);
//            	app_stage.show();
            	
            	
            }
            
			           
            
            
        }
        catch ( MalformedURLException ex ) {
            // a real program would need to handle this exception
        }
        catch ( IOException ex ) {
            // a real program would need to handle this exception
        }
	}

	
	
	@Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        System.out.println("LoginController.initialize()");
//        assert loginBtn != null : "fx:id=\"loginBtn\" was not injected: check your FXML file 'login.fxml'.";
        // initialize your logic here: all @FXML variables will have been injected
        
    }
}
