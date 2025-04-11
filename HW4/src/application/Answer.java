package application;

public class Answer {
    private int id;
    private int questionId;
    private String answerText;
    private String author;     // Who answered the question
    private boolean isRead;    // For tracking unread answers
    private boolean isSolution; // Flag if this answer resolves the question

    public Answer(int id, int questionId, String answerText, String author) {
        this.id = id;
        this.questionId = questionId;
        setAnswerText(answerText);
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author name cannot be empty.");
        }
        this.author = author;
        this.isRead = false;
        this.isSolution = false;
    }
    
    public int getId() {
        return id;
    }
    
    public int getQuestionId() {
        return questionId;
    }
    
    public String getAnswerText() {
        return answerText;
    }
    
    public String getAuthor() {
        return author;
    }
    
    // Input validation: answer text cannot be null or empty.
    public void setAnswerText(String answerText) {
        if (answerText == null || answerText.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer text cannot be empty.");
        }
        this.answerText = answerText;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void markAsRead() {
        this.isRead = true;
    }
    
    public boolean isSolution() {
        return isSolution;
    }
    
    public void markAsSolution() {
        this.isSolution = true;
    }
    
    @Override
    public String toString() {
        return "Answer [id=" + id + ", questionId=" + questionId + ", answerText=" + answerText +
               ", author=" + author + ", isRead=" + isRead + 
               (isSolution ? ", [SOLUTION]" : "") + "]";
    }
}
