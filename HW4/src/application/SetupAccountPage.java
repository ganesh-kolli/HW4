package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, and a valid invitation code to register.
 */
public class SetupAccountPage {
	
    private final DatabaseHelper databaseHelper;
    // DatabaseHelper to handle database operations.
    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
    	// Input fields for userName, password, and invitation code
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter InvitationCode");
        inviteCodeField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button setupButton = new Button("Setup");
        setupButton.setOnAction(a -> {
            // Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String code = inviteCodeField.getText();

            // Validate userName using UserNameRecognizer
            String userNameValidationError = UserNameRecognizer.checkForValidUserName(userName);
            if (!userNameValidationError.isEmpty()) {
                errorLabel.setText("Invalid userName: " + userNameValidationError);
                return;
            }

            // Validate password using PasswordEvaluator
            String passwordValidationError = PasswordEvaluator.evaluatePassword(password);
            if (!passwordValidationError.isEmpty()) {
                errorLabel.setText("Invalid password: " + passwordValidationError);
                return;
            }

            try {
                // Check if the user already exists
                if (!databaseHelper.doesUserExist(userName)) {
                    // Validate the invitation code
                    if (databaseHelper.validateInvitationCode(code)) {
                        // new : Get the role linked to the invitation code
                        String role = databaseHelper.getRoleFromInvitationCode(code);
                        if (role == null || role.isEmpty()) {
                            errorLabel.setText("Error: Role not associated with this code.");
                            return;
                        }

                        // Register user with role from code
                        User user = new User(userName, password, role);
                        databaseHelper.register(user);

                        // Navigate to welcome page
                        new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                    } else {
                        errorLabel.setText("The invitation code is invalid or has expired.");
                    }
                } else {
                    errorLabel.setText("This userName is taken! Please use another to set up an account.");
                }

            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Back button - now appears below the setup button
        Button backButton = new Button("Back to Setup/Login");
        backButton.setOnAction(e -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, inviteCodeField, setupButton, backButton, errorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
