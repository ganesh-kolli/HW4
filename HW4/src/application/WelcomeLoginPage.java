package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.*;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
    
    private final DatabaseHelper databaseHelper;

    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    public void show(Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // Set welcome message based on role.
        String welcomeMessage = "Welcome!!";
        if ("student".equals(user.getRole())) {
        	 welcomeMessage = "Welcome Student!!";
        }
        if ("user".equals(user.getRole())) {
       	 welcomeMessage = "Welcome User!!";
       }
        if ("Reviewer".equals(user.getRole())) {
       	 welcomeMessage = "Welcome Reviewer!!";
       }
        if ("Instructor".equals(user.getRole())) {
       	 welcomeMessage = "Welcome Instructor!!";
       }
        if ("admin".equals(user.getRole())) {
            welcomeMessage = "Welcome Admin!!";
        }
        Label welcomeLabel = new Label(welcomeMessage);
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Button to navigate to the user's respective page.
        Button continueButton = new Button("Continue to your Page");
        continueButton.setOnAction(a -> {
            String role = user.getRole();
            System.out.println(role);
            
            if(role.equals("admin")) {
                new AdminHomePage(databaseHelper).show(primaryStage);
            } else if(role.equals("user")) {
                new UserHomePage(databaseHelper).show(primaryStage);
            } else if (role.equalsIgnoreCase("student") || role.equalsIgnoreCase("reviewer")) {
                new StudentHomePage(databaseHelper).show(primaryStage);
            } else if(role.equals("instructor")) {
            	new InstructorHomePage(databaseHelper).show(primaryStage);
            } 
            
        });
        
        // Logout button.
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(a -> {
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });
        
        if ("admin".equals(user.getRole())) {
            // Admin-specific: Invite button.
            Button inviteButton = new Button("Invite");
            inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, primaryStage, user);
            });
            
            // Admin-specific: Switch Roles menu button.
            MenuButton switchRolesButton = new MenuButton("Switch Roles");
            MenuItem adminItem = new MenuItem("Admin");
            adminItem.setOnAction(a -> {
                DatabaseHelper.setSessionRole("admin");
                new AdminHomePage(databaseHelper).show(primaryStage);
            });

            MenuItem userItem = new MenuItem("User");
            userItem.setOnAction(a -> {
                DatabaseHelper.setSessionRole("user");
                new UserHomePage(databaseHelper).show(primaryStage);
            });

            MenuItem studentItem = new MenuItem("Student");
            studentItem.setOnAction(a -> {
                DatabaseHelper.setSessionRole("student");
                new StudentHomePage(databaseHelper).show(primaryStage);
            });

            MenuItem reviewerItem = new MenuItem("Reviewer");
            reviewerItem.setOnAction(a -> {
                DatabaseHelper.setSessionRole("reviewer");
                new StudentHomePage(databaseHelper).show(primaryStage);
            });

            MenuItem instructorItem = new MenuItem("Instructor");
            instructorItem.setOnAction(a -> {
                DatabaseHelper.setSessionRole("instructor");
                new InstructorHomePage(databaseHelper).show(primaryStage);
            });

            switchRolesButton.getItems().addAll(adminItem, userItem, studentItem, reviewerItem, instructorItem);
            

            // For admin, add welcome label, then admin-specific buttons, then common buttons.
            layout.getChildren().addAll(welcomeLabel, inviteButton, switchRolesButton, continueButton, logoutButton);
        } else {
            // For non-admin, add just the welcome label and common buttons.
            layout.getChildren().addAll(welcomeLabel, continueButton, logoutButton);
        }
        
        Scene welcomeScene = new Scene(layout, 800, 400);
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome Page");
    }
}
