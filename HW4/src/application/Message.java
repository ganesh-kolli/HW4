package application;

import java.time.LocalDateTime;

/**
 * Represents a private message exchanged between a reviewer and an author related to a review.
 */
public class Message {
    private int id;
    private int reviewId;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime sentAt;

    public Message(int id, int reviewId, String sender, String receiver, String content, LocalDateTime sentAt) {
        this.id = id;
        this.reviewId = reviewId;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.sentAt = sentAt;
    }

    public Message(int reviewId, String sender, String receiver, String content) {
        this.reviewId = reviewId;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.sentAt = LocalDateTime.now();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public int getReviewId() {
        return reviewId;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    @Override
    public String toString() {
        return "[" + sentAt.toString() + "] " + sender + ": " + content;
    }
}
