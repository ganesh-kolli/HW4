package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

import java.util.List;

public class ManageUserRolesPage {
    private final DatabaseHelper databaseHelper;

    public ManageUserRolesPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label titleLabel = new Label("Manage User Roles");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        List<String[]> usersAndRoles = databaseHelper.getAllUsernamesAndRoles();
        for (String[] user : usersAndRoles) {
            HBox userBox = new HBox(10);
            Label userNameLabel = new Label(user[0] + " - " + user[1]);
            ComboBox<String> roleComboBox = new ComboBox<>();
            roleComboBox.getItems().addAll("admin", "student", "instructor", "staff", "reviewer");
            roleComboBox.setValue(user[1]);

            Button updateButton = new Button("Update Role");
            updateButton.setOnAction(e -> {
                String newRole = roleComboBox.getValue();
                if (user[0].equals("admin") && !newRole.equals("admin")) {
                    showAlert("Error", "You cannot remove the admin role from your own account.");
                } else if (newRole.equals("admin") || databaseHelper.countAdmins() > 1) {
                    databaseHelper.updateUserRole(user[0], newRole);
                    userNameLabel.setText(user[0] + " - " + newRole);
                    showAlert("Success", "Role updated successfully.");
                } else {
                    showAlert("Error", "There must always be at least one admin user.");
                }
            });

            userBox.getChildren().addAll(userNameLabel, roleComboBox, updateButton);
            layout.getChildren().add(userBox);
        }

        Button backButton = new Button("Back to Manage Users");
        backButton.setOnAction(e -> new ListUsersPage(databaseHelper).show(primaryStage));
        layout.getChildren().add(backButton); 

        primaryStage.setScene(new Scene(layout, 800, 600));
        primaryStage.setTitle("Manage User Roles");
        primaryStage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}