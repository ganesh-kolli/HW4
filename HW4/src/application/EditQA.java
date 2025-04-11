package application;
import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;
/**
* EditQA allows students to edit their submitted questions and answers.
*/
public class EditQA {
   private final DatabaseHelper databaseHelper;
   private final String studentUsername;
   public EditQA(DatabaseHelper databaseHelper, String studentUsername) {
       this.databaseHelper = databaseHelper;
       this.studentUsername = studentUsername;
   }
   public void show(Stage primaryStage) {
       VBox layout = new VBox(10);
       layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
       Label titleLabel = new Label("Edit Your Questions & Answers");
       titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
       ListView<String> qaList = new ListView<>();
       TextArea editField = new TextArea();
       editField.setPromptText("Edit your selected question or answer here...");
       editField.setWrapText(true);
       Button updateButton = new Button("Update");
       Button backButton = new Button("Back");
       Label statusLabel = new Label();
       // Load student's questions and answers
       try {
           List<String[]> questions = databaseHelper.getQuestionsByUser(studentUsername);
           List<String[]> answers = databaseHelper.getAnswersByUser(studentUsername);
          
           for (String[] question : questions) {
               qaList.getItems().add("Q: " + question[1] + " (ID: " + question[0] + ")");
           }
           for (String[] answer : answers) {
               qaList.getItems().add("A: " + answer[1] + " (ID: " + answer[0] + ")");
           }
       } catch (SQLException e) {
           statusLabel.setText("Error retrieving data.");
           e.printStackTrace();
       }
       // Handle selection and load content into edit field
       qaList.setOnMouseClicked(event -> {
           String selected = qaList.getSelectionModel().getSelectedItem();
           if (selected != null) {
               editField.setText(selected.substring(3, selected.lastIndexOf(" (ID: "))); // Extract text
           }
       });
       // Handle update action
       updateButton.setOnAction(e -> {
           String selected = qaList.getSelectionModel().getSelectedItem();
           if (selected == null) {
               statusLabel.setText("Please select an item to edit.");
               return;
           }
           String newText = editField.getText().trim();
           if (newText.isEmpty()) {
               statusLabel.setText("Text cannot be empty.");
               return;
           }
           try {
               int id = Integer.parseInt(selected.substring(selected.lastIndexOf(" (ID: ") + 5, selected.length() - 1));
               if (selected.startsWith("Q: ")) {
                   databaseHelper.updateQuestion(id, newText);
               } else {
                   databaseHelper.updateAnswer(id, newText);
               }
               statusLabel.setText("Update successful.");
               qaList.getItems().set(qaList.getSelectionModel().getSelectedIndex(), selected.substring(0, 3) + newText + " (ID: " + id + ")");
           } catch (SQLException | NumberFormatException ex) {
               statusLabel.setText("Error updating item.");
               ex.printStackTrace();
           }
       });
       // Back button
       backButton.setOnAction(e -> new StudentHomePage(databaseHelper).show(primaryStage));
       layout.getChildren().addAll(titleLabel, qaList, editField, updateButton, statusLabel, backButton);
       primaryStage.setScene(new Scene(layout, 800, 500));
       primaryStage.setTitle("Edit Q&A");
   }
}