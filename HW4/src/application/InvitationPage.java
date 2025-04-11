package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * InvitationPage class represents the page where an admin can generate an invitation code.
 * The invitation code is displayed upon clicking a button.
 */
public class InvitationPage {

    /**
     * Displays the Invite Page in the provided primary stage.
     *
     * @param databaseHelper An instance of DatabaseHelper to handle database operations.
     * @param primaryStage   The primary stage where the scene will be displayed.
     * @param user           The current User instance to pass to the WelcomeLoginPage.
     */
    public void show(DatabaseHelper databaseHelper, Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label userLabel = new Label("Invite");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ComboBox<String> roleSelector = new ComboBox<>();
        roleSelector.getItems().addAll("student", "reviewer", "user", "instructor", "staff"); // ✅ added staff
        roleSelector.setPromptText("Select Role for Invite");

        Button showCodeButton = new Button("Generate Invitation Code");

        Label inviteCodeLabel = new Label("");
        inviteCodeLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");

        showCodeButton.setOnAction(a -> {
            String selectedRole = roleSelector.getValue();
            if (selectedRole == null || selectedRole.isEmpty()) {
                inviteCodeLabel.setText("Please select a role before generating a code.");
                return;
            }
            String invitationCode = databaseHelper.generateInvitationCode(selectedRole);
            inviteCodeLabel.setText("Code for " + selectedRole + ": " + invitationCode);
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            WelcomeLoginPage welcomePage = new WelcomeLoginPage(databaseHelper);
            welcomePage.show(primaryStage, user);
        });

        layout.getChildren().addAll(userLabel, roleSelector, showCodeButton, inviteCodeLabel, backButton);
        Scene inviteScene = new Scene(layout, 800, 400);
        primaryStage.setScene(inviteScene);
        primaryStage.setTitle("Invite Page");
    }
}
