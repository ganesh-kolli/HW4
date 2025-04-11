package application;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Answers {
    private List<Answer> answerList;
    
    public Answers() {
        answerList = new ArrayList<>();
    }
    
    // Create: Add a new answer.
    public void addAnswer(Answer answer) {
        if (answer == null) {
            throw new IllegalArgumentException("Answer cannot be null.");
        }
        answerList.add(answer);
    }
    
    // Read: Return all answers.
    public List<Answer> getAllAnswers() {
        return new ArrayList<>(answerList);
    }
    
    // Update: Update answer text.
    public boolean updateAnswer(int id, String newAnswerText) {
        for (Answer a : answerList) {
            if (a.getId() == id) {
                a.setAnswerText(newAnswerText);
                return true;
            }
        }
        return false;
    }
    
    // Delete: Remove an answer by its id.
    public boolean deleteAnswer(int id) {
        return answerList.removeIf(a -> a.getId() == id);
    }
    
    // Search: Return a subset of answers based on a predicate.
    public List<Answer> searchAnswers(Predicate<Answer> predicate) {
        return answerList.stream()
                         .filter(predicate)
                         .collect(Collectors.toList());
    }
    
    // Retrieve all answers associated with a particular question.
    public List<Answer> getAnswersForQuestion(int questionId) {
        return answerList.stream()
                         .filter(a -> a.getQuestionId() == questionId)
                         .collect(Collectors.toList());
    }
    
    // Count the number of unread answers for a given question.
    public int getUnreadCountForQuestion(int questionId) {
        return (int) answerList.stream()
                               .filter(a -> a.getQuestionId() == questionId && !a.isRead())
                               .count();
    }
    
    // Retrieve an answer by its id.
    public Answer getAnswerById(int id) {
        return answerList.stream()
                         .filter(a -> a.getId() == id)
                         .findFirst().orElse(null);
    }
    
    // For a given question, return a list of unique reviewers (authors of answers).
    public List<String> getReviewersForQuestion(int questionId) {
        return answerList.stream()
                         .filter(a -> a.getQuestionId() == questionId)
                         .map(Answer::getAuthor)
                         .distinct()
                         .collect(Collectors.toList());
    }
}
