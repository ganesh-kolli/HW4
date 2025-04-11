package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin.
 */
public class AdminHomePage {
    private final DatabaseHelper databaseHelper;

    public AdminHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label adminLabel = new Label("Hello, Admin!");
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // === Manage Users Button ===
        Button manageUsersButton = new Button("Manage Users");
        manageUsersButton.setOnAction(event -> {
            new ListUsersPage(databaseHelper).show(primaryStage);
        });

        // === Invite User Button ===
        Button inviteButton = new Button("Invite New User");
        inviteButton.setOnAction(event -> showInviteDialog());

        layout.getChildren().addAll(adminLabel, manageUsersButton, inviteButton);
        Scene adminScene = new Scene(layout, 800, 400);
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Admin Page");
    }

    private void showInviteDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Invite User");
        dialog.setHeaderText("Generate an Invitation Code");

        Label roleLabel = new Label("Select Role:");
        ComboBox<String> roleDropdown = new ComboBox<>();
        roleDropdown.getItems().addAll("Admin", "User", "Student", "Reviewer", "Instructor", "Staff");
        roleDropdown.setValue("Staff");

        VBox content = new VBox(10, roleLabel, roleDropdown);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return roleDropdown.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(role -> {
            String code = databaseHelper.generateInvitationCode(role);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Invitation Code");
            alert.setHeaderText("Share this code with the user:");
            alert.setContentText(code);
            alert.showAndWait();
        });
    }
}
