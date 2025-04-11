package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;

/**
 * The StaffHomePage class provides the staff dashboard interface
 * to monitor questions, answers, private feedback, and support instructors.
 */
public class StaffHomePage {
    private Stage stage;
    private DatabaseHelper dbHelper;

    /**
     * Constructor for the StaffHomePage class.
     * Connects to the database and initializes the stage.
     *
     * @param stage The JavaFX stage to be used for displaying this page.
     */
    public StaffHomePage(Stage stage) {
        this.stage = stage;
        this.dbHelper = new DatabaseHelper();
        try {
            dbHelper.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to connect to database: " + e.getMessage());
        }
    }

    /**
     * Displays the staff dashboard interface.
     */
    public void show() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Staff Dashboard");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold");

        Button viewQuestionsBtn = new Button("View All Questions & Answers");
        Button viewFeedbackBtn = new Button("View Private Feedback");
        Button addNoteBtn = new Button("Add Note for Instructor");
        Button searchBtn = new Button("Search Content");

        TextArea outputArea = new TextArea();
        outputArea.setPrefHeight(300);

        viewQuestionsBtn.setOnAction(e -> {
            List<String> results = dbHelper.getAllQuestionsAndAnswers();
            outputArea.setText(String.join("\n", results));
        });

        viewFeedbackBtn.setOnAction(e -> {
            List<String> feedbacks = dbHelper.getAllPrivateFeedback();
            outputArea.setText(String.join("\n", feedbacks));
        });

        addNoteBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Instructor Note");
            dialog.setHeaderText("Enter a note for the instructor");
            dialog.showAndWait().ifPresent(note -> {
                dbHelper.addInstructorNote(note);
                outputArea.setText("Note added successfully.");
            });
        });

        searchBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Search Content");
            dialog.setHeaderText("Enter keyword to search in Q&A");
            dialog.showAndWait().ifPresent(keyword -> {
                List<String> results = dbHelper.searchContentByKeyword(keyword);
                outputArea.setText(String.join("\n", results));
            });
        });

        root.getChildren().addAll(title, viewQuestionsBtn, viewFeedbackBtn, addNoteBtn, searchBtn, outputArea);

        Scene scene = new Scene(root, 600, 500);
        stage.setScene(scene);
        stage.setTitle("Staff Home Page");
        stage.show();
    }

    /**
     * Displays an error alert with the given message.
     *
     * @param message The error message to show.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
