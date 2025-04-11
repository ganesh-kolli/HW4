package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ReviewPage {
    private TableView<Review> reviewTable = new TableView<>();
    private TextArea contentArea = new TextArea();
    private ComboBox<String> questionCombo = new ComboBox<>();
    private ComboBox<String> answerCombo = new ComboBox<>();
    private Button addButton = new Button("Add Review");
    private Button updateButton = new Button("Update");
    private Button deleteButton = new Button("Delete");
    private Button messageButton = new Button("Open Chat");
    private final DatabaseHelper databaseHelper;
    private final String currentReviewerId;

    public ReviewPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.currentReviewerId = DatabaseHelper.getCurrentUsername();
    }

    public void start(Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        HBox inputBox = new HBox(10);
        try {
            List<String[]> questions = databaseHelper.getAllQuestions();
            for (String[] q : questions) {
                questionCombo.getItems().add("Q" + q[0] + ": " + q[1]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        questionCombo.setPromptText("Select Question");
        answerCombo.setPromptText("(Optional) Select Answer");

        questionCombo.setOnAction(e -> {
            answerCombo.getItems().clear();
            String selected = questionCombo.getValue();
            if (selected != null) {
                int qid = Integer.parseInt(selected.substring(1, selected.indexOf(":")));
                try {
                    List<String[]> answers = databaseHelper.getAnswersByQuestion(qid);
                    for (String[] a : answers) {
                        answerCombo.getItems().add("A" + a[0] + ": " + a[1]);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        inputBox.getChildren().addAll(questionCombo, answerCombo);

        HBox buttonBox = new HBox(10, addButton, updateButton, deleteButton, messageButton);

        root.getChildren().addAll(
                new Label("Your Reviews"), reviewTable,
                new Label("Review Details"), inputBox,
                contentArea, buttonBox
        );

        setupTable();
        setupActions(stage);
        refreshReviews();

        stage.setTitle("Reviewer Tab");
        stage.setScene(new Scene(root, 600, 500));
        stage.show();
    }

    private void setupTable() {
        TableColumn<Review, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTargetType()));

        TableColumn<Review, String> targetCol = new TableColumn<>("Target ID");
        targetCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getTargetId())));

        TableColumn<Review, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getContent()));

        reviewTable.getColumns().addAll(typeCol, targetCol, contentCol);
        reviewTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        reviewTable.setOnMouseClicked(e -> {
            Review selected = reviewTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                contentArea.setText(selected.getContent());
            }
        });
    }

    private void setupActions(Stage parentStage) {
        addButton.setOnAction(e -> {
            String selectedQuestion = questionCombo.getValue();
            String selectedAnswer = answerCombo.getValue();
            String reviewText = contentArea.getText().trim();

            if (selectedQuestion == null || reviewText.isEmpty()) {
                showAlert("Please select a question and write a review.");
                return;
            }

            try {
                int targetId;
                String type;
                if (selectedAnswer != null) {
                    targetId = Integer.parseInt(selectedAnswer.substring(1, selectedAnswer.indexOf(":")));
                    type = "answer";
                } else {
                    targetId = Integer.parseInt(selectedQuestion.substring(1, selectedQuestion.indexOf(":")));
                    type = "question";
                }

                Review review = new Review(currentReviewerId, type, targetId, reviewText);
                databaseHelper.insertReview(review);
                refreshReviews();
                contentArea.clear();
                answerCombo.getItems().clear();
            } catch (Exception ex) {
                showAlert("Error adding review: " + ex.getMessage());
            }
        });

        updateButton.setOnAction(e -> {
            Review selected = reviewTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setContent(contentArea.getText());
                selected.setUpdatedAt(LocalDateTime.now());
                try {
                    databaseHelper.updateReview(selected);
                    refreshReviews();
                } catch (Exception ex) {
                    showAlert("Error updating review: " + ex.getMessage());
                }
            }
        });

        deleteButton.setOnAction(e -> {
            Review selected = reviewTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    databaseHelper.deleteReview(selected.getId());
                    refreshReviews();
                    contentArea.clear();
                } catch (Exception ex) {
                    showAlert("Error deleting review: " + ex.getMessage());
                }
            }
        });

        messageButton.setOnAction(e -> {
            Review selected = reviewTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                new MessageDialog(databaseHelper, selected, currentReviewerId).show();
            } else {
                showAlert("Please select a review to open the chat.");
            }
        });
    }

    private void refreshReviews() {
        try {
            List<Review> reviews = databaseHelper.getReviewsByReviewer(currentReviewerId);
            reviewTable.getItems().setAll(reviews);
        } catch (Exception ex) {
            showAlert("Failed to load reviews: " + ex.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}