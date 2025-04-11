package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

//import list, database, and java.util.Optional
import java.util.List;
import java.util.Optional;
import databasePart1.DatabaseHelper;


/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays Usernames and Roles as well as the functionality of deleting users
 */

public class ListUsersPage {
	/**
     * Displays the admin page in the list users stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
	
	//call database for username and role info
	private final DatabaseHelper databaseHelper;
	
	public ListUsersPage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}
	
    public void show(Stage primaryStage) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // label to display for manage user page
	    Label manageUsersLabel = new Label("Manage Users");
	    manageUsersLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    //Text area where user info will be displayed//
	    TextArea listText = new TextArea();
	    listText.setEditable(false);
	    listText.setPrefHeight(200);
	    listText.setPromptText("All Current Users and Roles");
	    
	    // button to display list of users //
	    Button listUsersButton = new Button("List Users and Roles");
	    listUsersButton.setOnAction(event -> {
	    		List<String[]> usersAndRoles = databaseHelper.getAllUsernamesAndRoles();
	    		StringBuilder displayText = new StringBuilder();
	    		for (String[] user : usersAndRoles) { //call usersAndRoles from database and add info to string//
	    			displayText.append("Username: ").append(user[0]).append(" | ").append("Role: ").append(user[1])
	    			.append(" | ").append("Name: ----").append(" | ").append("Email: --------").append("\n");
	    		}
	    		listText.setText(displayText.toString());
	    });
	    //input field for deleting users(type username of account you want to delete)
	    TextField deleteUserField = new TextField();
	    deleteUserField.setPromptText("Enter Username of account to delete");
	    
	    // delete users button 
	    Button deleteUsersButton = new Button("Delete User");
	    deleteUsersButton.setOnAction(event -> {
	    	String usernameToDelete = deleteUserField.getText().trim();	    	
	        if (usernameToDelete.isEmpty()) {
	            showError("Please enter a username to delete."); //error displayed if deleteUserField is left empty//
	            return; 
	        } if (!databaseHelper.doesUserExist(usernameToDelete)) {
                showError("User does not exist.");
                return;
            } if (databaseHelper.getUserRole(usernameToDelete).equals("admin")) { //error to display if admin tries to delete their own account
	            showError("Cannot remove own admin access"); 
	            return;
	        }
	        // add alert to confirm user deletion
	        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Are you sure you want to delete this user?");
            confirmAlert.setContentText("User: " + usernameToDelete);
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                databaseHelper.deleteUser(usernameToDelete);     // User confirmed deletion, display success message//
                showSuccess("User deleted successfully.");
                deleteUserField.clear(); // Clear input field after deletion
            } else {
                showSuccess("User deletion canceled.");   //user cancelled deletion//
            }
	    });
	    
		//Div ee
		// Button to Manage User Roles
		Button manageRolesButton = new Button("Manage User Roles");
		manageRolesButton.setOnAction(event -> {new ManageUserRolesPage(databaseHelper).show(primaryStage);
		});
		
		//back button to take users back to admin home page 
        Button backButton = new Button("Back to Admin Home Page");
        backButton.setOnAction(e -> new AdminHomePage(databaseHelper).show(primaryStage));
	    
	    // .addAll, adds labels, buttons, and list to scene  //
	    layout.getChildren().addAll(manageUsersLabel, listUsersButton, listText, deleteUserField, 
	    	deleteUsersButton, manageRolesButton, backButton);
	    Scene adminScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(adminScene);
	    primaryStage.setTitle("Manage Users");
    }
// add success and error alert methods to display after delete is pressed
	private void showError(String message) {
	    Alert alert = new Alert(Alert.AlertType.ERROR);
	    alert.setTitle("Error");
	    alert.setHeaderText(null);
	    alert.setContentText(message);
	    	alert.showAndWait();
	    }
	    
	private void showSuccess(String message) {
	    Alert alert = new Alert(Alert.AlertType.ERROR);
	    alert.setTitle("Success");
	    alert.setHeaderText(null);
	    alert.setContentText(message);
	    alert.showAndWait();
    }
}



