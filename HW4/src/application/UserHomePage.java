package application;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import javafx.scene.control.Button;
/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {

    private final DatabaseHelper databaseHelper;  // Store DatabaseHelper instance

    public UserHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, User!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Ganesh: Added logout button to allow users to log out and return to the login page
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(a -> {
            new UserLoginPage(databaseHelper).show(primaryStage);  // Pass DatabaseHelper instance
        });
        
	    layout.getChildren().addAll(userLabel, logoutButton);
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("User Page");
    	
    }
}