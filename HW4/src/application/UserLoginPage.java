package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import databasePart1.*;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */
public class UserLoginPage {
	
    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	// Input field for the user's userName, password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordOrOtpField = new PasswordField();
        passwordOrOtpField.setPromptText("Enter password or OTP");
        passwordOrOtpField.setMaxWidth(250);

        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button loginButton = new Button("Login");
        Button forgotPasswordButton = new Button("Forgot Password?");
        Button requestOtpButton = new Button("Request OTP");

        // Initially hide the Request OTP button
        requestOtpButton.setVisible(false);

        forgotPasswordButton.setOnAction(e -> {
            // When Forgot Password is clicked, show the Request OTP button
            requestOtpButton.setVisible(true);
        });

        requestOtpButton.setOnAction(e -> {
            String userName = userNameField.getText();
            if (userName.isEmpty()) {
                errorLabel.setText("Please enter your username to request OTP.");
            } else if (!databaseHelper.doesUserExist(userName)) {
                errorLabel.setText("User does not exist.");
            } else {
				String otp = databaseHelper.generateOTP(userName);  // Generate and store OTP
                errorLabel.setText("OTP has been generated. Please check with the admin.");
            }
        });

        loginButton.setOnAction(a -> {
            String userName = userNameField.getText();
            String passwordOrOtp = passwordOrOtpField.getText();

            // If user has clicked "Forgot Password," validate OTP instead of normal password
            if (requestOtpButton.isVisible() && databaseHelper.hasOTP(userName)) {
                if (databaseHelper.validateOTP(userName, passwordOrOtp)) {
                    databaseHelper.clearOTP(userName);  // Clear the OTP after successful verification
                    ResetPasswordPage resetPasswordPage = new ResetPasswordPage(primaryStage, databaseHelper, userName);
                    resetPasswordPage.show();  // Redirect to reset password page
                } else {
                    errorLabel.setText("Invalid OTP or OTP has expired.");
                }
            } else {
                // Normal login flow with username and password
                try {
                    String role = databaseHelper.getUserRole(userName);
                    if (role != null && databaseHelper.login(new User(userName, passwordOrOtp, role))) {
                    	DatabaseHelper.setSessionRole(role); 
                        WelcomeLoginPage welcomeLoginPage = new WelcomeLoginPage(databaseHelper);
                        welcomeLoginPage.show(primaryStage, new User(userName, passwordOrOtp, role));
                    } else {
                        errorLabel.setText("Incorrect username or password.");
                    }
                } catch (Exception ex) {
                    errorLabel.setText("An error occurred: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        // Layout setup
        VBox layout = new VBox(10, userNameField, passwordOrOtpField, loginButton, forgotPasswordButton, requestOtpButton, errorLabel);
       

        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        primaryStage.setScene(new Scene(layout, 800, 400));
    }
}