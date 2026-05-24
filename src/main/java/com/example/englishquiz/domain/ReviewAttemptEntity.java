package com.example.englishquiz.domain;

import com.example.englishquiz.enums.ReviewRating;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "review_attempts")
public class ReviewAttemptEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private CardEntity card;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_state_id", nullable = false)
    private CardStateEntity cardState;

    @Column(name = "user_answer", nullable = false)
    private String userAnswer;

    @Column(name = "expected_answer", nullable = false)
    private String expectedAnswer;

    @Column(name = "normalized_user_answer", nullable = false)
    private String normalizedUserAnswer;

    @Column(name = "normalized_expected_answer", nullable = false)
    private String normalizedExpectedAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", nullable = false)
    private ReviewRating rating;

    @Column(name = "ai_feedback")
    private String aiFeedback;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUserEntity getUser() { return user; }
    public void setUser(AppUserEntity user) { this.user = user; }
    public CardEntity getCard() { return card; }
    public void setCard(CardEntity card) { this.card = card; }
    public CardStateEntity getCardState() { return cardState; }
    public void setCardState(CardStateEntity cardState) { this.cardState = cardState; }
    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
    public String getExpectedAnswer() { return expectedAnswer; }
    public void setExpectedAnswer(String expectedAnswer) { this.expectedAnswer = expectedAnswer; }
    public String getNormalizedUserAnswer() { return normalizedUserAnswer; }
    public void setNormalizedUserAnswer(String normalizedUserAnswer) { this.normalizedUserAnswer = normalizedUserAnswer; }
    public String getNormalizedExpectedAnswer() { return normalizedExpectedAnswer; }
    public void setNormalizedExpectedAnswer(String normalizedExpectedAnswer) { this.normalizedExpectedAnswer = normalizedExpectedAnswer; }
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
    public ReviewRating getRating() { return rating; }
    public void setRating(ReviewRating rating) { this.rating = rating; }
    public String getAiFeedback() { return aiFeedback; }
    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }
    public Integer getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Integer responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
}
