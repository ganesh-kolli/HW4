package application;

import databasePart1.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ResetPasswordPage {
    private Stage stage;
    private DatabaseHelper dbHelper;
    private String userName;

    public ResetPasswordPage(Stage stage, DatabaseHelper dbHelper, String userName) {
        this.stage = stage;
        this.dbHelper = dbHelper;
        this.userName = userName;
    }

    public void show() {
        Label label = new Label("Reset Your Password");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Set New Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Retype New Password");
        Button confirmButton = new Button("Confirm and Logout");

        // Label to display error messages for invalid input or issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        confirmButton.setOnAction(e -> {
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Step 1: Check if passwords match
            if (!newPassword.equals(confirmPassword)) {
                errorLabel.setText("Passwords do not match! Try again.");
                return;
            }

            // Step 2: Evaluate the password using PasswordEvaluator
            String passwordValidationError = PasswordEvaluator.evaluatePassword(newPassword);
            if (!passwordValidationError.isEmpty()) {
                errorLabel.setText("Invalid password: " + passwordValidationError);
                return;
            }

            // Step 3: If validation passes, update the password in the database
            dbHelper.updatePassword(userName, newPassword);
            dbHelper.clearOTP(userName); // Clear OTP after password reset

            label.setText("Password reset successfully! Logging out...");

            // Step 4: Redirect to the login page
            UserLoginPage loginPage = new UserLoginPage(dbHelper);
            loginPage.show(stage);
        });

        VBox layout = new VBox(10, label, newPasswordField, confirmPasswordField, confirmButton, errorLabel);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
    }
}
