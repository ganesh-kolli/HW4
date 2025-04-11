package application;

import databasePart1.DatabaseHelper;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentHomePage {

    private DatabaseHelper databaseHelper;

    // Shared lists for Q&A
    /**
     * The StudentHomePage class provides the main user interface for student users.
     * It displays various tabs that allow students to ask questions, view and answer questions,
     * search through questions and answers, manage Q&A content, and communicate with reviewers.
     *
     * <p>This class interacts with a DatabaseHelper instance to perform all necessary CRUD
     * operations on questions, answers, reviews, messages, and trusted reviewers.</p>
     *
     * <p><b>Fields:</b></p>
     * <ul>
     *     <li>{@code databaseHelper} - The DatabaseHelper instance used for database operations.</li>
     *     <li>{@code unresolvedQuestionsList} - List of unresolved questions.</li>
     *     <li>{@code allQuestionsList} - List of all questions.</li>
     *     <li>{@code resolvedQuestionsList} - List of resolved questions.</li>
     *     <li>{@code answersList} - List of answers for questions.</li>
     *     <li>{@code followUpQuestionsList} - List of follow-up questions.</li>
     *     <li>{@code currentUsername} - The username of the currently logged-in student.</li>
     *     <li>{@code globalThreadList} and {@code globalChatBox} - Global UI components for the Messages tab.</li>
     *     <li>{@code threadList} and {@code chatBox} - UI components for displaying and sending messages.</li>
     *     <li>{@code trustedReviewersListView} - ListView to display trusted reviewers.</li>
     * </ul>
     *
     * @author 
     * @version 1.0
     */
    ObservableList<String> unresolvedQuestionsList = FXCollections.observableArrayList();
    ObservableList<String> allQuestionsList = FXCollections.observableArrayList();
    public ObservableList<String> resolvedQuestionsList = FXCollections.observableArrayList();
    ObservableList<String> answersList = FXCollections.observableArrayList();
    ObservableList<String> followUpQuestionsList = FXCollections.observableArrayList();

    // current username (Brian)
    private String currentUsername;

    // Global UI components used by the Messages tab for dynamic updates
    private ListView<String> globalThreadList;
    private VBox globalChatBox;

    // Instance variables for the Messages tab (so they can be accessed in multiple methods)
    private ListView<String> threadList;
    private VBox chatBox;

    // NEW for Task 5: A ListView to show the student’s “trusted reviewers”
    private ListView<String> trustedReviewersListView;

    // Constructor
    /**
     * Constructs a new StudentHomePage instance.
     *
     * @param databaseHelper the DatabaseHelper instance used to interact with the database.
     */
    public StudentHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.currentUsername = DatabaseHelper.getCurrentUsername();
        // Optional: you can respond to changes in answersList if needed:
        answersList.addListener((javafx.collections.ListChangeListener<String>) change -> {
            // ...
        });
    }

    // A helper method to combine a question and its answers (for search or display)
    
    /**
     * Combines a question and its associated answers into a single formatted string.
     *
     * @param question the question to combine with its answers.
     * @return a string containing the question followed by its answers.
     */
    private String combineQA(String question) {
        StringBuilder sb = new StringBuilder();
        sb.append(question).append("\n");
        for (String answer : answersList) {
            String prefix = question + " | Answer: ";
            if (answer.startsWith(prefix)) {
                sb.append("- ").append(answer.substring(prefix.length())).append("\n");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Displays the Student Home Page UI.
     *
     * <p>This method connects to the database, populates question lists,
     * creates all the tabs for various functionalities (e.g., Ask Question, Search, etc.),
     * and sets up the overall scene.</p>
     *
     * @param stage the primary Stage for the application.
     */
    public void show(Stage stage) {
        // Connect to the database.
        try {
            databaseHelper.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Populate allQuestionsList and unresolvedQuestionsList from DB
        try {
            List<String[]> questions = databaseHelper.getAllQuestions();
            allQuestionsList.clear();
            unresolvedQuestionsList.clear();  // so they don't duplicate on logout/login

            for (String[] question : questions) {
                String formatted = "Q" + question[0] + ": " + question[1];
                allQuestionsList.add(formatted);

                // Add to unresolvedQuestionsList only if not already resolved
                if (!resolvedQuestionsList.contains(formatted)) {
                    unresolvedQuestionsList.add(formatted);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create the TabPane with all the tabs
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
            createAskQuestionTab(),
            createUnresolvedQuestionsTab(),
            createAnswerQuestionTab(),
            createAllQATab(),
            createSearchTab(),
            createDeleteTab(),
            createEditQATab(),
            createResolvedQuestionsTab(),
            createFollowUpTab(),
            createMessagesTab(),
            createReviewerRequestTab(),
            createReviewWeightageTab(),
            createViewReviewsTab()// Weightage
        );

        // If the session role is "reviewer", add the Reviewer tab.
        String role = DatabaseHelper.getSessionRole();
        System.out.println("Current role: " + role);
        if ("reviewer".equalsIgnoreCase(role)) {
            tabPane.getTabs().add(createReviewerTab());
        }

        // NEW: Add the Trusted Reviewers tab for all students:
        tabPane.getTabs().add(createTrustedReviewersTab());

        // Set up the overall layout.
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        Label headerLabel = new Label("Student Home Page");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        root.setTop(headerLabel);
        BorderPane.setAlignment(headerLabel, Pos.CENTER);
        root.setCenter(tabPane);

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> stage.close());

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            DatabaseHelper.setSessionRole(null);
            new SetupLoginSelectionPage(databaseHelper).show(new Stage());
            stage.close();
        });
        HBox bottomBox = new HBox(20, exitButton, logoutButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Student Home Page");
        stage.show();
    }

    // ------------------------- Existing Tabs -------------------------

    // Tab 1: Ask Question
    /**
     * Creates the "Ask Question" tab.
     *
     * @return a {@code Tab} object for asking questions.
     */
    private Tab createAskQuestionTab() {
        Tab tab = new Tab("Ask Question");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER_LEFT);

        Label instruction = new Label("Enter your question:");
        TextField questionField = new TextField();
        questionField.setPromptText("Type your question here...");
        Button submitButton = new Button("Submit Question");
        Label feedbackLabel = new Label();

        submitButton.setOnAction(e -> {
            String questionText = questionField.getText().trim();
            if (questionText.isEmpty()) {
                feedbackLabel.setText("Question cannot be empty.");
            } else {
                int id;
                try {
                    id = databaseHelper.insertQuestion(questionText, "student");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    feedbackLabel.setText("Error submitting question to the database.");
                    return;
                }
                String baseQuestion = "Q" + id + ": " + questionText;
                feedbackLabel.setText("Question submitted: " + baseQuestion);
                questionField.clear();
                unresolvedQuestionsList.add(baseQuestion);
                allQuestionsList.add(baseQuestion);
            }
        });

        layout.getChildren().addAll(instruction, questionField, submitButton, feedbackLabel);
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

    // Tab 2: Unresolved Questions
    /**
     * Creates the "Unresolved Questions" tab.
     *
     * @return a {@code Tab} object for displaying unresolved questions.
     */
    private Tab createUnresolvedQuestionsTab() {
        Tab tab = new Tab("Unresolved Questions");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label header = new Label("List of Unresolved Questions:");
        ListView<String> questionListView = new ListView<>(unresolvedQuestionsList);
        questionListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedQuestion = questionListView.getSelectionModel().getSelectedItem();
                if (selectedQuestion != null) {
                    openQuestionThreadDialog(selectedQuestion, true);
                }
            }
        });

        layout.getChildren().addAll(header, questionListView);
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

    // Helper: Open a dialog showing question thread and its answers
    /**
     * Opens a dialog showing the thread for a selected question,
     * including its answers and follow-up answers.
     *
     * @param selectedQuestion the selected question.
     * @param canResolve indicates whether the question can be marked as resolved.
     */
    private void openQuestionThreadDialog(String selectedQuestion, boolean canResolve) {
        Stage dialog = new Stage();
        dialog.setTitle("Question Thread: " + selectedQuestion);

        String[] parts = selectedQuestion.split(": ", 2);
        String questionID = (parts.length > 0) ? parts[0].trim() : "Q?";
        String questionText = (parts.length > 1) ? parts[1].trim() : "";

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        Label headingLabel = new Label("Question " + questionID + ": " + questionText);
        headingLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        root.getChildren().add(headingLabel);

        Accordion answersAccordion = new Accordion();
        List<String> rawEntries = getRawEntriesForQuestion(selectedQuestion);
        for (String raw : rawEntries) {
            String[] segments = raw.split("\\|");
            if (segments.length < 2) continue;
            String mainAnswerText = segments[1].trim().replace("Answer:", "").trim();
            TitledPane mainAnswerPane = new TitledPane(mainAnswerText, null);
            VBox mainAnswerContent = new VBox(10);
            mainAnswerContent.setPadding(new Insets(10));

            Accordion followUpAccordion = new Accordion();
            int idx = 2;
            while (idx < segments.length) {
                String seg = segments[idx].trim();
                if (seg.startsWith("Follow-Up:")) {
                    String followUpQ = seg.replace("Follow-Up:", "").trim();
                    List<String> followUpAnswers = new ArrayList<>();
                    idx++;
                    while (idx < segments.length) {
                        String nextSeg = segments[idx].trim();
                        if (nextSeg.startsWith("Follow-Up:")) {
                            idx--;
                            break;
                        } else if (nextSeg.startsWith("Answer:")) {
                            String ans = nextSeg.replace("Answer:", "").trim();
                            followUpAnswers.add(ans);
                        } else {
                            idx--;
                            break;
                        }
                        idx++;
                    }
                    TitledPane followUpPane = new TitledPane(followUpQ, null);
                    VBox followUpContent = new VBox(5);
                    followUpContent.setPadding(new Insets(10));
                    for (String fAns : followUpAnswers) {
                        followUpContent.getChildren().add(new Label(fAns));
                    }
                    followUpPane.setContent(followUpContent);
                    followUpAccordion.getPanes().add(followUpPane);
                }
                idx++;
            }
            mainAnswerContent.getChildren().add(followUpAccordion);
            mainAnswerPane.setContent(mainAnswerContent);
            answersAccordion.getPanes().add(mainAnswerPane);
        }

        if (canResolve) {
            Button resolveButton = new Button("Mark as Resolved");
            resolveButton.setOnAction(e -> {
                resolvedQuestionsList.add(selectedQuestion);
                unresolvedQuestionsList.remove(selectedQuestion);
                dialog.close();
            });
            root.getChildren().add(resolveButton);
        }

        root.getChildren().add(answersAccordion);
        Scene scene = new Scene(root, 600, 600);
        dialog.setScene(scene);
        dialog.show();
    }

    // Helper: Retrieve raw entries for a given question from answersList and followUpQuestionsList
    /**
     * Retrieves raw entries (answers and follow-up questions) associated with a given question.
     *
     * @param question the question text.
     * @return a {@code List<String>} containing raw entries.
     */
    private List<String> getRawEntriesForQuestion(String question) {
        List<String> results = new ArrayList<>();
        int colonIndex = question.indexOf(":");
        if (colonIndex < 0) return results;
        String qidKey = question.substring(0, colonIndex).trim();
        for (String ans : answersList) {
            if (ans.startsWith(qidKey)) results.add(ans);
        }
        for (String followUpEntry : followUpQuestionsList) {
            if (followUpEntry.startsWith(qidKey)) results.add(followUpEntry);
        }
        return results;
    }

    // Tab 3: Answer a Question
    /**
     * Creates the "Answer a Question" tab.
     *
     * @return a {@code Tab} object for answering questions.
     */
    private Tab createAnswerQuestionTab() {
        Tab tab = new Tab("Answer a Question");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label instruction = new Label("Select a question to answer:");
        ComboBox<String> questionCombo = new ComboBox<>(unresolvedQuestionsList);
        Label orLabel = new Label("OR");
        orLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label followUpInstruction = new Label("Select a follow-up question to answer:");
        ComboBox<String> followUpCombo = new ComboBox<>();
        Label linkedAnswerLabel = new Label("Related Answer:");
        linkedAnswerLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        TextArea linkedAnswerArea = new TextArea();
        linkedAnswerArea.setWrapText(true);
        linkedAnswerArea.setEditable(false);
        linkedAnswerArea.setVisible(false);
        TextField answerField = new TextField();
        answerField.setPromptText("Type your answer here...");
        Button submitAnswerButton = new Button("Submit Answer");
        Label feedback = new Label();

        questionCombo.setOnAction(e -> {
            String selectedQuestion = questionCombo.getValue();
            followUpCombo.getItems().clear();
            linkedAnswerArea.clear();
            linkedAnswerArea.setVisible(false);
            if (selectedQuestion != null) {
                for (String followUp : followUpQuestionsList) {
                    if (followUp.contains(selectedQuestion.trim()) && followUp.contains(" | Follow-Up: ")) {
                        followUpCombo.getItems().add(followUp);
                    }
                }
            }
        });

        followUpCombo.setOnAction(e -> {
            String selectedFollowUp = followUpCombo.getValue();
            if (selectedFollowUp != null) {
                String[] parts = selectedFollowUp.split(" \\| Follow-Up: ");
                if (parts.length > 1) {
                    String relatedAnswer = parts[0].replace(" | Answer:", "").trim();
                    linkedAnswerArea.setText(relatedAnswer);
                    linkedAnswerArea.setVisible(true);
                }
            }
        });

        submitAnswerButton.setOnAction(e -> {
            String mainQuestion = questionCombo.getValue();
            String followUpQuestion = followUpCombo.getValue();
            String answerText = answerField.getText().trim();
            String username = DatabaseHelper.getCurrentUsername();
            if ((mainQuestion == null && followUpQuestion == null) || answerText.isEmpty()) {
                feedback.setText("Please select a question (or follow-up) and provide an answer.");
            } else {
                if (followUpQuestion != null) {
                    answersList.remove(followUpQuestion);
                    String mergedEntry = followUpQuestion + " | Answer: " + answerText + " by " + username;
                    answersList.add(mergedEntry);
                    followUpQuestionsList.remove(followUpQuestion);
                } else {
                    int colonIndex = mainQuestion.indexOf(":");
                    int questionId = Integer.parseInt(mainQuestion.substring(1, colonIndex).trim());
                    try {
                        databaseHelper.insertAnswer(questionId, username, answerText);
                        answersList.add(mainQuestion + " | Answer: " + answerText + " by " + username);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        feedback.setText("Error inserting answer into the database.");
                        return;
                    }
                }
                feedback.setText("Answer submitted successfully!");
                answerField.clear();
                followUpCombo.getItems().clear();
                linkedAnswerArea.clear();
                linkedAnswerArea.setVisible(false);
            }
        });

        layout.getChildren().addAll(
            instruction, questionCombo, orLabel, followUpInstruction, followUpCombo,
            linkedAnswerLabel, linkedAnswerArea, answerField, submitAnswerButton, feedback
        );
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

    // Tab 4: All Q&A
    /**
     * Creates the "All Q&A" tab which displays all questions.
     *
     * @return a {@code Tab} object for displaying all questions and their answers.
     */
    private Tab createAllQATab() {
        Tab tab = new Tab("All Q&A");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        Label header = new Label("All Asked Questions and Their Answers:");
        ListView<String> allQAListView = new ListView<>(allQuestionsList);
        allQAListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedQuestion = allQAListView.getSelectionModel().getSelectedItem();
                if (selectedQuestion != null) {
                    openQuestionThreadDialog(selectedQuestion, false);
                }
            }
        });
        layout.getChildren().addAll(header, allQAListView);
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

    // Tab 5: Search
    /**
     * Creates the "Search" tab that allows users to search for questions by keyword.
     *
     * @return a {@code Tab} object for searching questions.
     */
    private Tab createSearchTab() {
        Tab tab = new Tab("Search");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        Label instruction = new Label("Enter a keyword to search for questions:");
        TextField keywordField = new TextField();
        keywordField.setPromptText("Enter keyword...");
        Button searchButton = new Button("Search");
        TextArea searchResults = new TextArea();
        searchResults.setEditable(false);
        searchResults.setWrapText(true);

        searchButton.setOnAction(e -> {
            String keyword = keywordField.getText().trim().toLowerCase();
            if (keyword.isEmpty()) {
                searchResults.setText("Please enter a keyword.");
            } else {
                StringBuilder results = new StringBuilder();
                results.append("Search Results for '").append(keyword).append("':\n");
                boolean found = false;
                for (String entry : allQuestionsList) {
                    if (entry.toLowerCase().contains(keyword)) {
                        results.append(entry).append("\n");
                        found = true;
                    }
                }
                if (!found) results.append("No matching questions found.\n");
                searchResults.setText(results.toString());
            }
        });

        layout.getChildren().addAll(instruction, keywordField, searchButton, searchResults);
        tab.setContent(layout);
        return tab;
    }

    // Tab 6: Delete Q&A
    /**
     * Creates the "Delete Q&A" tab for deleting questions.
     *
     * @return a {@code Tab} object for deleting questions.
     */
    private Tab createDeleteTab() {
        Tab tab = new Tab("Delete Q&A");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        Label header = new Label("Select a question to delete:");
        ListView<String> deleteListView = new ListView<>(allQuestionsList);
        Button deleteButton = new Button("Delete Selected");
        Label feedbackLabel = new Label();

        deleteListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedQuestion = deleteListView.getSelectionModel().getSelectedItem();
                if (selectedQuestion != null) {
                    openQuestionThreadDialog(selectedQuestion, false);
                }
            }
        });

        deleteButton.setOnAction(e -> {
            String selectedQuestion = deleteListView.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                allQuestionsList.remove(selectedQuestion);
                unresolvedQuestionsList.remove(selectedQuestion);
                resolvedQuestionsList.remove(selectedQuestion);
                answersList.removeIf(answer -> answer.startsWith(selectedQuestion + " | Answer: "));
                feedbackLabel.setText("Deleted: " + selectedQuestion);
            } else {
                feedbackLabel.setText("Please select a question to delete.");
            }
        });

        layout.getChildren().addAll(header, deleteListView, deleteButton, feedbackLabel);
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

    // Tab 7: Resolved Questions
    /**
     * Creates the "Resolved Questions" tab which displays questions marked as resolved.
     *
     * @return a {@code Tab} object for displaying resolved questions.
     */
    private Tab createResolvedQuestionsTab() {
        Tab tab = new Tab("Resolved Questions");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        Label header = new Label("List of Resolved Questions:");
        ListView<String> resolvedListView = new ListView<>(resolvedQuestionsList);
        resolvedListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedQuestion = resolvedListView.getSelectionModel().getSelectedItem();
                if (selectedQuestion != null) openQuestionThreadDialog(selectedQuestion, false);
            }
        });
        layout.getChildren().addAll(header, resolvedListView);
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

    // Tab 8: Ask a Follow-Up Question
    /**
     * Creates the "Ask a Follow-Up Question" tab.
     *
     * @return a {@code Tab} object for asking follow-up questions.
     */
    private Tab createFollowUpTab() {
        Tab tab = new Tab("Ask a Follow-Up Question");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        Label instruction = new Label("Select an answer and ask a follow-up question");
        ComboBox<String> answeredQuestionCombo = new ComboBox<>();
        TextField followUpQuestionField = new TextField();
        followUpQuestionField.setPromptText("Type your follow-up question here");
        Button submitFollowUpButton = new Button("Submit Question");
        Label feedbackLabel = new Label();

        answeredQuestionCombo.setOnMouseClicked(e -> {
            answeredQuestionCombo.getItems().clear();
            answeredQuestionCombo.getItems().addAll(answersList);
        });

        submitFollowUpButton.setOnAction(e -> {
            String selectedAnswer = answeredQuestionCombo.getValue();
            String followUpText = followUpQuestionField.getText().trim();
            if (selectedAnswer == null || followUpText.isEmpty()) {
                feedbackLabel.setText("Please select an answer and enter a follow-up question.");
            } else {
                answersList.remove(selectedAnswer);
                String mergedEntry = selectedAnswer + " | Follow-Up: " + followUpText;
                followUpQuestionsList.add(mergedEntry);
                feedbackLabel.setText("Follow-up question submitted successfully (merged with original answer).");
                followUpQuestionField.clear();
            }
        });

        layout.getChildren().addAll(instruction, answeredQuestionCombo, followUpQuestionField, submitFollowUpButton, feedbackLabel);
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

    // Tab 9: Edit Q&A
    /**
     * Creates the "Edit Q&A" tab to allow users to update questions and answers.
     *
     * @return a {@code Tab} object for editing Q&A content.
     */
    private Tab createEditQATab() {
        Tab tab = new Tab("Edit Q&A");
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        Label titleLabel = new Label("Edit Your Questions & Answers");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<String> questionList = new ListView<>();
        ListView<String> answerList = new ListView<>();
        TextArea editField = new TextArea();
        editField.setPromptText("Edit your selected question or answer here...");
        editField.setWrapText(true);
        Button updateButton = new Button("Update");
        Label statusLabel = new Label();

        try {
            List<String[]> questions = databaseHelper.getAllQuestions();
            for (String[] question : questions) {
                questionList.getItems().add("Q" + question[0] + ": " + question[1]);
            }
            List<String[]> answers = databaseHelper.getAllAnswers();
            for (String[] ans : answers) {
                answerList.getItems().add("A" + ans[0] + ": " + ans[1]);
            }
        } catch (SQLException e) {
            statusLabel.setText("Error retrieving data.");
            e.printStackTrace();
        }

        questionList.setOnMouseClicked(event -> {
            answerList.getSelectionModel().clearSelection();
            String selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                editField.setText(selected.substring(selected.indexOf(":") + 2));
            }
        });

        answerList.setOnMouseClicked(event -> {
            questionList.getSelectionModel().clearSelection();
            String selected = answerList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                editField.setText(selected.substring(selected.indexOf(":") + 2));
            }
        });

        updateButton.setOnAction(e -> {
            String selectedQuestion = questionList.getSelectionModel().getSelectedItem();
            String selectedAnswer = answerList.getSelectionModel().getSelectedItem();
            String newText = editField.getText().trim();
            if (newText.isEmpty()) {
                statusLabel.setText("Text cannot be empty.");
                return;
            }
            try {
                if (selectedQuestion != null) {
                    int id = Integer.parseInt(selectedQuestion.split(":")[0].substring(1));
                    databaseHelper.updateQuestion(id, newText);
                    int selectedIndex = questionList.getSelectionModel().getSelectedIndex();
                    String updatedQuestion = "Q" + id + ": " + newText;
                    questionList.getItems().set(selectedIndex, updatedQuestion);
                    for (int i = 0; i < allQuestionsList.size(); i++) {
                        String entry = allQuestionsList.get(i);
                        if (entry.startsWith("Q" + id + ":")) {
                            int answerIndex = entry.indexOf(" | Answer:");
                            if (answerIndex != -1) {
                                allQuestionsList.set(i, updatedQuestion + entry.substring(answerIndex));
                            } else {
                                allQuestionsList.set(i, updatedQuestion);
                            }
                        }
                    }
                    for (int i = 0; i < unresolvedQuestionsList.size(); i++) {
                        String entry = unresolvedQuestionsList.get(i);
                        if (entry.startsWith("Q" + id + ":")) {
                            unresolvedQuestionsList.set(i, updatedQuestion);
                        }
                    }
                    statusLabel.setText("Question updated successfully.");
                } else if (selectedAnswer != null) {
                    int answerId = Integer.parseInt(selectedAnswer.split(":")[0].substring(1));
                    databaseHelper.updateAnswer(answerId, newText);
                    int selectedIndex = answerList.getSelectionModel().getSelectedIndex();
                    String updatedAnswer = "A" + answerId + ": " + newText;
                    answerList.getItems().set(selectedIndex, updatedAnswer);
                    int questionId = databaseHelper.getQuestionIdByAnswerId(answerId);
                    for (int i = 0; i < allQuestionsList.size(); i++) {
                        String entry = allQuestionsList.get(i);
                        if (entry.startsWith("Q" + questionId + ":")) {
                            int answerIdx = entry.indexOf(" | Answer:");
                            String questionPart = (answerIdx != -1) ? entry.substring(0, answerIdx) : entry;
                            allQuestionsList.set(i, questionPart + " | Answer: " + newText);
                        }
                    }
                    statusLabel.setText("Answer updated successfully.");
                } else {
                    statusLabel.setText("Please select an item to edit.");
                }
            } catch (SQLException | NumberFormatException ex) {
                statusLabel.setText("Error updating.");
                ex.printStackTrace();
            }
        });

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e2 -> {
            questionList.getItems().clear();
            answerList.getItems().clear();
            try {
                List<String[]> questions = databaseHelper.getAllQuestions();
                for (String[] question : questions) {
                    questionList.getItems().add("Q" + question[0] + ": " + question[1]);
                }
                List<String[]> answers = databaseHelper.getAllAnswers();
                for (String[] ans : answers) {
                    answerList.getItems().add("A" + ans[0] + ": " + ans[1]);
                }
            } catch (SQLException ex) {
                statusLabel.setText("Error retrieving data.");
                ex.printStackTrace();
            }
        });

        HBox listsContainer = new HBox(20, questionList, answerList);
        listsContainer.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(titleLabel, listsContainer, editField, updateButton, refreshButton, statusLabel);
        tab.setContent(layout);
        return tab;
    }

    // Tab 10: Messages Tab – displays messages dynamically and allows sending replies.
    /**
     * Creates the "Messages" tab which displays messages between students and reviewers.
     *
     * @return a {@code Tab} object for messaging.
     */
    private Tab createMessagesTab() {
        // Initialize instance variables for global access
        threadList = new ListView<>();
        chatBox = new VBox(10);
        globalThreadList = threadList;
        globalChatBox = chatBox;

        refreshThreadList();

        Tab tab = new Tab("Messages");
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));
        Label title = new Label("Messages from Reviewers");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        layout.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        VBox leftPane = new VBox(10, new Label("Reviewed Items:"), threadList);
        leftPane.setPadding(new Insets(10));
        leftPane.setPrefWidth(250);

        chatBox.setPadding(new Insets(10));
        chatBox.setAlignment(Pos.TOP_LEFT);
        ScrollPane chatScroll = new ScrollPane(chatBox);
        chatScroll.setFitToWidth(true);

        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Write your reply...");
        replyArea.setPrefRowCount(3);
        Button sendBtn = new Button("Send Reply");
        HBox replyRow = new HBox(10, replyArea, sendBtn);
        replyRow.setAlignment(Pos.CENTER_LEFT);

        VBox rightPane = new VBox(10, chatScroll, replyRow);
        rightPane.setPadding(new Insets(10));
        VBox.setVgrow(chatScroll, Priority.ALWAYS);

        layout.setLeft(leftPane);
        layout.setCenter(rightPane);

        // Load threads from the database
        String student = DatabaseHelper.getCurrentUsername();
        try {
            List<Review> reviews = databaseHelper.getReviewsAboutStudent(student);
            for (Review r : reviews) {
                String reviewedItemText = ("question".equalsIgnoreCase(r.getTargetType()))
                        ? databaseHelper.getQuestionTextById(r.getTargetId())
                        : databaseHelper.getAnswerTextById(r.getTargetId());
                threadList.getItems().add("Review ID: " + r.getId() + " | " + reviewedItemText);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        threadList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            int reviewId = Integer.parseInt(newVal.substring("Review ID: ".length(), newVal.indexOf(" | ")));
            refreshChatBox(chatBox, reviewId);
        });

        sendBtn.setOnAction(e -> {
            String selectedThread = threadList.getSelectionModel().getSelectedItem();
            String replyText = replyArea.getText().trim();
            if (selectedThread == null || replyText.isEmpty()) return;
            int reviewId = Integer.parseInt(selectedThread.substring("Review ID: ".length(), selectedThread.indexOf(" | ")));
            String studentUser = DatabaseHelper.getCurrentUsername();
            try {
                List<Message> thread = databaseHelper.getMessagesForReview(reviewId);
                String reviewer;
                if (!thread.isEmpty()) {
                    String firstSender = thread.get(0).getSender();
                    reviewer = studentUser.equals(firstSender) ? thread.get(0).getReceiver() : firstSender;
                } else {
                    reviewer = databaseHelper.getReviewerIdForReview(reviewId);
                }
                Message reply = new Message(0, reviewId, studentUser, reviewer, replyText, LocalDateTime.now());
                databaseHelper.insertMessage(reply);
                replyArea.clear();
                refreshChatBox(chatBox, reviewId);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // Auto-refresh the chat every 5 seconds.
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), ev -> {
            String selected = threadList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int reviewId = Integer.parseInt(selected.substring("Review ID: ".length(), selected.indexOf(" | ")));
                refreshChatBox(chatBox, reviewId);
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

    /**
     * Refreshes the list of threads in the Messages tab.
     */
    public void refreshThreadList() {
        threadList.getItems().clear();
        String student = DatabaseHelper.getCurrentUsername();
        try {
            List<Review> reviews = databaseHelper.getReviewsAboutStudent(student);
            for (Review r : reviews) {
                String reviewedItemText = ("question".equalsIgnoreCase(r.getTargetType()))
                        ? databaseHelper.getQuestionTextById(r.getTargetId())
                        : databaseHelper.getAnswerTextById(r.getTargetId());
                threadList.getItems().add("Review ID: " + r.getId() + " | " + reviewedItemText);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Refreshes the chat box with messages for a given review.
     *
     * @param chatBox the VBox containing the chat messages.
     * @param reviewId the ID of the review.
     */
    // Helper: Refresh chat box with messages for a given review.
    private void refreshChatBox(VBox chatBox, int reviewId) {
        chatBox.getChildren().clear();
        try {
            List<Message> messages = databaseHelper.getMessagesForReview(reviewId);
            for (Message msg : messages) {
                Label msgLabel = new Label(msg.getSender() + " ➤ " + msg.getReceiver() + "\n" + msg.getContent());
                msgLabel.setWrapText(true);
                msgLabel.setStyle("-fx-padding: 6; -fx-background-color: lightgray; -fx-background-radius: 6;");
                chatBox.getChildren().add(msgLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper: Update global messages tab (if needed by other parts of the app)
    /**
     * Updates the global messages tab with the latest messages for a given review.
     *
     * @param reviewId the ID of the review.
     */
    private void updateGlobalMessagesTab(int reviewId) {
        if (globalThreadList == null || globalChatBox == null) return;
        String selected = globalThreadList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            for (String item : globalThreadList.getItems()) {
                if (item.contains("Review ID: " + reviewId)) {
                    globalThreadList.getSelectionModel().select(item);
                    refreshChatBox(globalChatBox, reviewId);
                    break;
                }
            }
        } else if (selected.contains("Review ID: " + reviewId)) {
            refreshChatBox(globalChatBox, reviewId);
        }
    }

    // Tab: Request Reviewer Role (for students)
    /**
     * Creates the "Request Reviewer Role" tab for students to request a reviewer role.
     *
     * @return a {@code Tab} object for reviewer requests.
     */
    private Tab createReviewerRequestTab() {
        Tab tab = new Tab("Request Reviewer Role");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        Label header = new Label("Request to be a reviewer:");
        Button requestButton = new Button("Request Reviewer Role");
        requestButton.setOnAction(e -> {
            databaseHelper.sendReviewerRequest(currentUsername);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Reviewer request sent by " + currentUsername + ".");
            alert.showAndWait();
        });
        layout.getChildren().addAll(header, requestButton);
        tab.setContent(layout);
        tab.setClosable(true);
        return tab;
    }

    // Weightage Tab: Review Weights — Allows student to set weightage values for reviews
    /**
     * Creates the "Review Weights" tab that allows students to assign weightage to reviews.
     *
     * @return a {@code Tab} object for setting review weightage.
     */
    private Tab createReviewWeightageTab() {
        Tab tab = new Tab("Review Weights");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label label = new Label("Assign weightage (0–100) to a review:");
        ComboBox<Review> reviewDropdown = new ComboBox<>();
        TextField weightField = new TextField();
        weightField.setPromptText("Enter weight (e.g., 75)");

        Button saveButton = new Button("Save");
        Label status = new Label();

        // Fetch reviews related to the current user
        try {
            List<Review> reviews = databaseHelper.getReviewsAboutStudent(currentUsername);
            reviewDropdown.getItems().addAll(reviews);
        } catch (SQLException e) {
            status.setText("Could not load reviews.");
            e.printStackTrace();
        }

        saveButton.setOnAction(e -> {
            Review selected = reviewDropdown.getValue();
            if (selected == null) {
                status.setText("Please select a review.");
                return;
            }
            try {
                int weight = Integer.parseInt(weightField.getText().trim());
                if (weight < 0 || weight > 100) {
                    status.setText("Weight must be between 0 and 100.");
                    return;
                }
                selected.setWeightage(weight);
                databaseHelper.updateReviewWeightage(selected.getId(), weight);  // Update weightage in DB
                status.setText("Weight updated successfully.");
            } catch (NumberFormatException ex) {
                status.setText("Invalid weight. Must be a number.");
            } catch (SQLException ex) {
                status.setText("Database error.");
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(label, reviewDropdown, weightField, saveButton, status);
        tab.setContent(layout);
        tab.setClosable(false);
        return tab;
    }

    // Tab: Reviewer Tab (for reviewers to manage reviews)
    /**
     * Creates the "Reviewer" tab for reviewers to manage their reviews.
     *
     * @return a {@code Tab} object for reviewers.
     */
    private Tab createReviewerTab() {
        Tab tab = new Tab("Reviewer");
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        Label header = new Label("Manage Your Reviews");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<Review> reviewTable = new TableView<>();
        reviewTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Review, String> itemCol = new TableColumn<>("Reviewed Item");
        itemCol.setCellValueFactory(data -> {
            Review r = data.getValue();
            String itemText = getReviewedItemText(r);
            return new SimpleStringProperty(itemText);
        });

        TableColumn<Review, String> reviewCol = new TableColumn<>("Review");
        reviewCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getContent()));

        reviewTable.getColumns().addAll(itemCol, reviewCol);

        ComboBox<String> questionDropdown = new ComboBox<>();
        ComboBox<String> answerDropdown = new ComboBox<>();
        questionDropdown.setPromptText("Select Question");
        answerDropdown.setPromptText("Select Answer (optional)");

        questionDropdown.setOnMouseClicked(e -> refreshReviewerDropdowns(questionDropdown, answerDropdown));
        questionDropdown.setOnAction(e -> {
            answerDropdown.getItems().clear();
            String selected = questionDropdown.getValue();
            if (selected == null || selected.isEmpty()) {
                showAlert("Please select a question first.");
                return;
            }
            try {
                int qid = Integer.parseInt(selected.split(":")[0].substring(1));
                List<String[]> answers = databaseHelper.getAnswersByQuestionId(qid);
                for (String[] a : answers) {
                    answerDropdown.getItems().add("A" + a[0] + ": " + a[1]);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Failed to load answers for the selected question.");
            }
        });

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter your review content here...");
        contentArea.setWrapText(true);

        Button addButton = new Button("Add Review");
        Button updateButton = new Button("Update");
        Button deleteButton = new Button("Delete");
        Button chatButton = new Button("Open Chat");

        reviewTable.setOnMouseClicked(e -> {
            Review selected = reviewTable.getSelectionModel().getSelectedItem();
            if (selected != null) contentArea.setText(selected.getContent());
        });

        addButton.setOnAction(e -> {
            String reviewText = contentArea.getText().trim();
            if (reviewText.isEmpty()) {
                showAlert("Please enter some review text before adding.");
                return;
            }
            try {
                String type = (answerDropdown.getValue() != null) ? "answer" : "question";
                String selected = (answerDropdown.getValue() != null) ? answerDropdown.getValue() : questionDropdown.getValue();
                int targetId = Integer.parseInt(selected.split(":")[0].substring(1));
                Review review = new Review(DatabaseHelper.getCurrentUsername(), type, targetId, reviewText);
                int reviewId = databaseHelper.insertReview(review);
                review.setId(reviewId);

                refreshReviews(reviewTable);
                contentArea.clear();

                // Add to Messages tab if not already present
                String reviewedItemText = getReviewedItemText(review);
                String threadEntry = "Review ID: " + review.getId() + " | " + reviewedItemText;
                if (!globalThreadList.getItems().contains(threadEntry)) {
                    globalThreadList.getItems().add(threadEntry);
                }

                // Also update chat box if needed
                updateGlobalMessagesTab(review.getId());
            } catch (Exception ex) {
                showAlert("Error adding review: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        updateButton.setOnAction(e -> {
            Review selected = reviewTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setContent(contentArea.getText().trim());
                selected.setUpdatedAt(LocalDateTime.now());
                try {
                    databaseHelper.updateReview(selected);
                    refreshReviews(reviewTable);
                } catch (SQLException ex) {
                    showAlert("Error updating review: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                showAlert("Please select a review to update.");
            }
        });

        deleteButton.setOnAction(e -> {
            Review selected = reviewTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    databaseHelper.deleteReview(selected.getId());
                    refreshReviews(reviewTable);
                    contentArea.clear();
                } catch (SQLException ex) {
                    showAlert("Error deleting review: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                showAlert("Please select a review to delete.");
            }
        });

        chatButton.setOnAction(e -> {
            Review selected = reviewTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Please select a review to chat about.");
                return;
            }
            Stage chatStage = new Stage();
            chatStage.setTitle("Private Chat for Review ID: " + selected.getId());
            VBox chatRoot = new VBox(10);
            chatRoot.setPadding(new Insets(10));
            TextArea chatArea = new TextArea();
            chatArea.setEditable(false);
            chatArea.setWrapText(true);
            TextArea inputBox = new TextArea();
            inputBox.setPromptText("Type your message...");
            inputBox.setPrefRowCount(3);
            Button sendBtn = new Button("Send");

            try {
                List<Message> messages = databaseHelper.getMessagesForReview(selected.getId());
                for (Message msg : messages) {
                    chatArea.appendText(msg.getSender() + " ➤ " + msg.getReceiver() + ": " + msg.getContent() + "\n");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            sendBtn.setOnAction(evt -> {
                String msg = inputBox.getText().trim();
                if (!msg.isEmpty()) {
                    try {
                        String sender = DatabaseHelper.getCurrentUsername();
                        String receiver = databaseHelper.getReviewerIdForReview(selected.getId());
                        if (sender.equals(receiver)) {
                            receiver = databaseHelper.getStudentIdForReview(selected.getId());
                        }
                        Message m = new Message(0, selected.getId(), sender, receiver, msg, LocalDateTime.now());
                        databaseHelper.insertMessage(m);
                        chatArea.appendText(sender + " ➤ " + receiver + ": " + msg + "\n");
                        inputBox.clear();

                        // Add to Messages tab if not already there
                        String reviewedItemText = getReviewedItemText(selected);
                        String threadEntry = "Review ID: " + selected.getId() + " | " + reviewedItemText;
                        if (!globalThreadList.getItems().contains(threadEntry)) {
                            globalThreadList.getItems().add(threadEntry);
                        }
                        updateGlobalMessagesTab(selected.getId());
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            chatRoot.getChildren().addAll(chatArea, inputBox, sendBtn);
            chatStage.setScene(new Scene(chatRoot, 500, 400));
            chatStage.show();
        });

        HBox dropdowns = new HBox(10, new Label("Select Question:"), questionDropdown, new Label("(Optional) Select Answer:"), answerDropdown);
        dropdowns.setAlignment(Pos.CENTER_LEFT);
        HBox buttonsBox = new HBox(10, addButton, updateButton, deleteButton, chatButton);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().addAll(header, reviewTable, dropdowns, new Label("Review Content:"), contentArea, buttonsBox);
        refreshReviews(reviewTable);

        tab.setContent(root);
        tab.setClosable(false);
        return tab;
    }

    // --- Additional helper methods for the Reviewer tab ---
    /**
     * Refreshes the dropdown lists in the Reviewer tab.
     *
     * @param questionDropdown the ComboBox for questions.
     * @param answerDropdown the ComboBox for answers.
     */
    private void refreshReviewerDropdowns(ComboBox<String> questionDropdown, ComboBox<String> answerDropdown) {
        questionDropdown.getItems().clear();
        answerDropdown.getItems().clear();
        try {
            List<String[]> questions = databaseHelper.getAllQuestions();
            for (String[] q : questions) {
                questionDropdown.getItems().add("Q" + q[0] + ": " + q[1]);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Refreshes the review weightage dropdown.
     *
     * @param reviewDropdown the ComboBox containing reviews.
     */
    private void refreshReviewWeightageDropdown(ComboBox<Review> reviewDropdown) {
        reviewDropdown.getItems().clear();
        try {
            List<Review> reviews = databaseHelper.getReviewsAboutStudent(currentUsername);
            reviewDropdown.getItems().addAll(reviews);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the reviewed item text (either question or answer) for a given review.
     *
     * @param review the {@code Review} object.
     * @return the text of the reviewed item.
     */

    private String getReviewedItemText(Review review) {
        if (review.getTargetType().equalsIgnoreCase("question")) {
            try {
                return databaseHelper.getQuestionTextById(review.getTargetId());
            } catch (SQLException e) {
                e.printStackTrace();
                return "(Error fetching question)";
            }
        } else {
            try {
                return databaseHelper.getAnswerTextById(review.getTargetId());
            } catch (SQLException e) {
                e.printStackTrace();
                return "(Error fetching answer)";
            }
        }
    }

    /**
     * Refreshes the TableView in the Reviewer tab with the latest reviews.
     *
     * @param reviewTable the TableView to update.
     */

    private void refreshReviews(TableView<Review> reviewTable) {
        reviewTable.getItems().clear();
        try {
            List<Review> reviews = databaseHelper.getReviewsByReviewer(DatabaseHelper.getCurrentUsername());
            reviewTable.getItems().addAll(reviews);
        } catch (SQLException ex) {
            showAlert("Failed to load reviews: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Displays an alert dialog with the specified message.
     *
     * @param msg the message to display.
     */
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }

    // ----------------------------------------------------------------
    // NEW TAB: Trusted Reviewers (Task 5)
    // ----------------------------------------------------------------
    /**
     * Creates the "Trusted Reviewers" tab to manage and search for trusted reviewers.
     *
     * @return a {@code Tab} object for trusted reviewers management.
     */
    private Tab createTrustedReviewersTab() {
        Tab tab = new Tab("Trusted Reviewers");
        tab.setClosable(false);

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));

        Label title = new Label("Manage Trusted Reviewers & Search Their Reviews");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        layout.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        // Left box: Add/Remove Trusted Reviewers
        VBox leftBox = new VBox(10);
        leftBox.setPadding(new Insets(10));

        Label allReviewersLabel = new Label("All Reviewers:");
        ListView<String> allReviewersList = new ListView<>();

        // Populate with all reviewers except current user
        try {
            List<String[]> allUsers = databaseHelper.getAllUsernamesAndRoles();
            for (String[] usr : allUsers) {
                // usr[0] = username, usr[1] = role
                if ("reviewer".equalsIgnoreCase(usr[1]) && !usr[0].equalsIgnoreCase(currentUsername)) {
                    allReviewersList.getItems().add(usr[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button addTrustedBtn = new Button("Add to Trusted");
        addTrustedBtn.setOnAction(e -> {
            String selectedReviewer = allReviewersList.getSelectionModel().getSelectedItem();
            if (selectedReviewer == null) {
                showAlert("Please select a reviewer to add.");
                return;
            }
            try {
                databaseHelper.addTrustedReviewer(currentUsername, selectedReviewer);
                refreshTrustedReviewersList();
            } catch (SQLException ex) {
                showAlert("Error adding trusted reviewer: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        Label trustedLabel = new Label("My Trusted Reviewers:");
        trustedReviewersListView = new ListView<>();
        refreshTrustedReviewersList();

        Button removeTrustedBtn = new Button("Remove Selected");
        removeTrustedBtn.setOnAction(e -> {
            String selected = trustedReviewersListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Please select a trusted reviewer to remove.");
                return;
            }
            try {
                databaseHelper.removeTrustedReviewer(currentUsername, selected);
                refreshTrustedReviewersList();
            } catch (SQLException ex) {
                showAlert("Error removing trusted reviewer: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        leftBox.getChildren().addAll(
            allReviewersLabel, allReviewersList, addTrustedBtn,
            new Separator(),
            trustedLabel, trustedReviewersListView, removeTrustedBtn
        );

        // Right box: Searching only among trusted reviewers
        VBox rightBox = new VBox(10);
        rightBox.setPadding(new Insets(10));

        Label searchLabel = new Label("Search Reviews (Trusted Only):");
        TextField keywordField = new TextField();
        keywordField.setPromptText("Enter search keyword...");
        Button searchBtn = new Button("Search");
        TextArea searchResults = new TextArea();
        searchResults.setEditable(false);
        searchResults.setWrapText(true);

        searchBtn.setOnAction(e -> {
            String keyword = keywordField.getText().trim();
            if (keyword.isEmpty()) {
                searchResults.setText("Please enter a keyword.");
                return;
            }
            try {
                List<Review> matchingReviews = databaseHelper.searchReviewsByTrustedReviewers(currentUsername, keyword);
                if (matchingReviews.isEmpty()) {
                    searchResults.setText("No reviews found from trusted reviewers for keyword: " + keyword);
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Reviews from Trusted Reviewers for '").append(keyword).append("':\n\n");
                    for (Review rev : matchingReviews) {
                        sb.append("Reviewer: ").append(rev.getReviewerId())
                          .append(" | Weight: ").append(rev.getWeightage())
                          .append("\nContent: ").append(rev.getContent())
                          .append("\n---\n");
                    }
                    searchResults.setText(sb.toString());
                }
            } catch (SQLException ex) {
                showAlert("Error searching reviews: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        rightBox.getChildren().addAll(searchLabel, keywordField, searchBtn, searchResults);

        SplitPane split = new SplitPane(leftBox, rightBox);
        layout.setCenter(split);

        tab.setContent(layout);
        return tab;
    }
    /**
     * Creates and returns the "View Reviews" tab.
     * <p>
     * This tab displays a ListView containing reviews for potential answers.
     * The reviews are loaded based on the current user's role. For a reviewer, the tab
     * shows reviews provided by them; for a student, it shows reviews for their questions or answers.
     * The tab also includes a "Message Reviewer" button to initiate messaging with the reviewer
     * of the selected review, and a "Refresh" button to re-query the database for the latest reviews.
     * </p>
     *
     * @return a {@code Tab} object representing the "View Reviews" tab.
     */
    private Tab createViewReviewsTab() {
        Tab tab = new Tab("View Reviews");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label header = new Label("Reviews for Potential Answers");
        
        // ListView to display reviews
        ListView<Review> reviewListView = new ListView<>();
        reviewListView.setCellFactory(lv -> new ListCell<Review>() {
            @Override
            protected void updateItem(Review item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Use helper methods getReviewerName() and getPreview() for display.
                    setText("Reviewer: " + item.getReviewerName() + " - " + item.getPreview());
                }
            }
        });

        // Refresh logic: load reviews based on current user's role.
        Runnable refreshReviews = () -> {
            try {
                String currentUser = DatabaseHelper.getCurrentUsername();
                String role = DatabaseHelper.getSessionRole();
                List<Review> reviews;
                if (role != null && role.equalsIgnoreCase("reviewer")) {
                    // For reviewers, load reviews they provided.
                    reviews = databaseHelper.getReviewsByReviewer(currentUser);
                } else {
                    // For students, load reviews for their questions or answers.
                    reviews = databaseHelper.getReviewsAboutStudent(currentUser);
                }
                System.out.println("Loaded reviews count: " + reviews.size());
                reviewListView.setItems(FXCollections.observableArrayList(reviews));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };

        // Initial load of reviews.
        refreshReviews.run();

        // Button to message the reviewer of the selected review.
        Button messageButton = new Button("Message Reviewer");
        messageButton.setOnAction(e -> {
            Review selectedReview = reviewListView.getSelectionModel().getSelectedItem();
            if (selectedReview == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please select a review to message.");
                alert.showAndWait();
            } else {
                MessageDialog dialog = new MessageDialog(databaseHelper, selectedReview, DatabaseHelper.getCurrentUsername());
                dialog.show();
            }
        });

        // NEW: Refresh button to re-query the database.
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshReviews.run());

        HBox buttonBox = new HBox(10, messageButton, refreshButton);
        buttonBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(header, reviewListView, buttonBox);
        tab.setContent(layout);
        tab.setClosable(false);
        
        return tab;
    }
    
    /**
     * Refreshes the "trusted reviewers" list for the current student.
     * <p>
     * This method clears the existing items in the trustedReviewersListView and repopulates it by querying
     * the database for the list of reviewers that the current student has marked as trusted.
     * If the trustedReviewersListView is not initialized (null), the method does nothing.
     * </p>
     */

    private void refreshTrustedReviewersList() {
        if (trustedReviewersListView == null) return;
        trustedReviewersListView.getItems().clear();
        try {
            List<String> trusted = databaseHelper.getTrustedReviewers(currentUsername);
            trustedReviewersListView.getItems().addAll(trusted);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
