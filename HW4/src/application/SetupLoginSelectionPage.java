package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import databasePart1.*;

/**
 * The SetupLoginSelectionPage class allows users to choose between setting up a new account
 * or logging into an existing account. It provides two buttons for navigation to the respective pages.
 */
public class SetupLoginSelectionPage {
	
    private final DatabaseHelper databaseHelper;

    public SetupLoginSelectionPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        
    	// Buttons to select Login / Setup options that redirect to respective pages
        Button setupButton = new Button("SetUp");
        Button loginButton = new Button("Login");
        
        setupButton.setOnAction(a -> {
            new SetupAccountPage(databaseHelper).show(primaryStage);
        });
        loginButton.setOnAction(a -> {
        	new UserLoginPage(databaseHelper).show(primaryStage);
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(setupButton, loginButton);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
    // Role Selection Interface for Multi-Role Users
    public void showRoleSelection(Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label roleLabel = new Label("Select Your Role:");
        ToggleGroup roleToggleGroup = new ToggleGroup();

        // Assuming roles are comma-separated in a String
        String[] roles = user.getRole().split(",");

        for (String role : roles) {
        	// This creates a radio button with the label being the role name (like "admin" or "user").
            RadioButton roleButton = new RadioButton(role.trim());// .trim ensures there is no extra spaces.
            roleButton.setToggleGroup(roleToggleGroup);
            layout.getChildren().add(roleButton);
        }

        Button continueButton = new Button("Continue");
        continueButton.setOnAction(e -> {
            RadioButton selectedRole = (RadioButton) roleToggleGroup.getSelectedToggle();
            if (selectedRole != null) {
                redirectToDashboard(primaryStage, user, selectedRole.getText());
            }
        });

        layout.getChildren().addAll(roleLabel, continueButton);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Role Selection");
        primaryStage.show();
    }

    // Redirect user to their respective pages.
    private void redirectToDashboard(Stage primaryStage, User user, String role) {
        if (role.equals("admin")) {
        	new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
        } else if (role.equals("user")) {
            new UserHomePage(databaseHelper).show(primaryStage);
        } // Additional roles can be added here as needed
    }
}