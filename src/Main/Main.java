package Main;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/Login/Login.fxml"));
        primaryStage.setTitle("FreshDrive");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
