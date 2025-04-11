package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class MessageDialog {
    private final DatabaseHelper databaseHelper;
    private final Review review;
    private final String currentUser;

    public MessageDialog(DatabaseHelper db, Review review, String currentUser) {
        this.databaseHelper = db;
        this.review = review;
        this.currentUser = currentUser;
    }

    public void show() {
        Stage dialog = new Stage();
        dialog.setTitle(review.getReviewerId() + " - reviewer");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        VBox messageBox = new VBox(8);
        messageBox.setPadding(new Insets(10));
        scrollPane.setContent(messageBox);

        // Load existing messages
        try {
            List<Message> messages = databaseHelper.getMessagesForReview(review.getId());
            for (Message msg : messages) {
                Label msgLabel = new Label(msg.getSender() + ": " + msg.getContent());
                msgLabel.setWrapText(true);
                msgLabel.setStyle("-fx-background-color: #E0E0E0; -fx-padding: 8; -fx-background-radius: 8;");
                messageBox.getChildren().add(msgLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        HBox inputRow = new HBox(10);
        TextField inputField = new TextField();
        inputField.setPromptText("Type a message...");
        inputField.setPrefWidth(400);

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                Message newMessage = new Message(review.getId(), currentUser, review.getReviewerId(), text);
                try {
                    databaseHelper.insertMessage(newMessage);
                    Label msgLabel = new Label(currentUser + ": " + text);
                    msgLabel.setWrapText(true);
                    msgLabel.setStyle("-fx-background-color: lightblue; -fx-padding: 8; -fx-background-radius: 8;");
                    messageBox.getChildren().add(msgLabel);
                    inputField.clear();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        inputRow.getChildren().addAll(inputField, sendButton);
        root.getChildren().addAll(scrollPane, inputRow);

        Scene scene = new Scene(root, 500, 400);
        dialog.setScene(scene);
        dialog.show();
    }
}
