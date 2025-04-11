package application;

public class Question {
    private int id;
    private String text;
    private boolean resolved;
    private int solutionAnswerId; // -1 if no solution has been chosen

    public Question(int id, String text) {
        this.id = id;
        setText(text);
        this.resolved = false;
        this.solutionAnswerId = -1;
    }
    
    public int getId() {
        return id;
    }
    
    public String getText() {
        return text;
    }
    
    // Input validation: question text cannot be null or empty.
    public void setText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Question text cannot be empty.");
        }
        this.text = text;
    }
    
    public boolean isResolved() {
        return resolved;
    }
    
    // Mark this question as resolved by specifying the answer ID that resolved it.
    public void markResolved(int answerId) {
        this.resolved = true;
        this.solutionAnswerId = answerId;
    }
    
    public int getSolutionAnswerId() {
        return solutionAnswerId;
    }
    
    @Override
    public String toString() {
        return "Question [id=" + id + ", text=" + text + ", resolved=" + resolved + 
               (resolved ? ", solutionAnswerId=" + solutionAnswerId : "") + "]";
    }
}
