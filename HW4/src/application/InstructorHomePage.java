package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

import databasePart1.DatabaseHelper;

public class InstructorHomePage extends Application {

    @SuppressWarnings("unused")
	private DatabaseHelper currentUsername;
    private DatabaseHelper databaseHelper;

    public InstructorHomePage(DatabaseHelper databaseHelper) {
        this.currentUsername = databaseHelper;
        this.databaseHelper = databaseHelper;
    }
    
    public void show(Stage primaryStage) {
    	start(primaryStage);
    }

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        Tab reviewerRequestsTab = new Tab("Reviewer Requests", createReviewerRequestsView());
        tabPane.getTabs().add(reviewerRequestsTab);
        
        Scene scene = new Scene(tabPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Instructor Home Page");
        primaryStage.show();
    }

    private VBox createReviewerRequestsView() {
        VBox vbox = new VBox();
        ListView<String> requestListView = new ListView<>();
        List<String> requests = databaseHelper.getReviewerRequests();
        requestListView.getItems().addAll(requests);
        
        requestListView.setOnMouseClicked(event -> {
            String selectedStudent = requestListView.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                promoteToReviewer(selectedStudent);
            }
        });
        
        vbox.getChildren().add(requestListView);
        return vbox;
    }

    private void promoteToReviewer(String studentUsername) {
        databaseHelper.promoteToReviewer(studentUsername);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, studentUsername + " is now a reviewer.");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
