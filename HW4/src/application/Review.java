// Review.java
package application;

import java.time.LocalDateTime;

public class Review {
    private int id;
    private String reviewerId;
    private String targetType; // "question" or "answer"
    private int targetId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int weightage; // NEW: to control display ordering

    public Review(int id, String reviewerId, String targetType, int targetId, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.reviewerId = reviewerId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.weightage = 0; // Weightage

    }

    public Review(String reviewerId, String targetType, int targetId, String content) {
        this(0, reviewerId, targetType, targetId, content, LocalDateTime.now(), LocalDateTime.now());
    }
    @Override
    public String toString() {
        return "[" + targetType + " ID " + targetId + "] " + content;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public int getTargetId() { return targetId; }
    public void setTargetId(int targetId) { this.targetId = targetId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    // Weightage
    public int getWeightage() { return weightage; }
    public void setWeightage(int weightage) { this.weightage = weightage; }
    
 // New method: Returns a preview (first 50 characters) of the review content.
    public String getPreview() {
        if (content == null) {
            return "";
        }
        return content.length() <= 50 ? content : content.substring(0, 50) + "...";
    }
    
    // New method: Returns the reviewer's name (using reviewerId by default).
    public String getReviewerName() {
        return reviewerId;
    }

}