package application;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Questions {
    private List<Question> questionList;
    
    public Questions() {
        questionList = new ArrayList<>();
    }
    
    // Create: Add a new question to the list.
    public void addQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Question cannot be null.");
        }
        questionList.add(question);
    }
    
    // Read: Return a list of all questions.
    public List<Question> getAllQuestions() {
        return new ArrayList<>(questionList);
    }
    
    // Return a list of unresolved questions.
    public List<Question> getUnresolvedQuestions() {
        return questionList.stream()
                           .filter(q -> !q.isResolved())
                           .collect(Collectors.toList());
    }
    
    // Update: Update the text of a question by its id.
    public boolean updateQuestion(int id, String newText) {
        for (Question q : questionList) {
            if (q.getId() == id) {
                q.setText(newText);
                return true;
            }
        }
        return false;
    }
    
    // Delete: Remove a question by its id.
    public boolean deleteQuestion(int id) {
        return questionList.removeIf(q -> q.getId() == id);
    }
    
    // Search: Return a subset of questions that satisfy a given predicate.
    public List<Question> searchQuestions(Predicate<Question> predicate) {
        return questionList.stream()
                           .filter(predicate)
                           .collect(Collectors.toList());
    }
}
