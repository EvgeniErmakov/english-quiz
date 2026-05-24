package com.example.englishquiz.domain;

import com.example.englishquiz.enums.CardStateStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "card_states")
public class CardStateEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private CardEntity card;

    @Column(name = "difficulty", nullable = false)
    private BigDecimal difficulty;

    @Column(name = "stability", nullable = false)
    private BigDecimal stability;

    @Column(name = "retrievability")
    private BigDecimal retrievability;

    @Column(name = "repetitions", nullable = false)
    private int repetitions;

    @Column(name = "lapses", nullable = false)
    private int lapses;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "next_review_at", nullable = false)
    private Instant nextReviewAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CardStateStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUserEntity getUser() { return user; }
    public void setUser(AppUserEntity user) { this.user = user; }
    public CardEntity getCard() { return card; }
    public void setCard(CardEntity card) { this.card = card; }
    public BigDecimal getDifficulty() { return difficulty; }
    public void setDifficulty(BigDecimal difficulty) { this.difficulty = difficulty; }
    public BigDecimal getStability() { return stability; }
    public void setStability(BigDecimal stability) { this.stability = stability; }
    public BigDecimal getRetrievability() { return retrievability; }
    public void setRetrievability(BigDecimal retrievability) { this.retrievability = retrievability; }
    public int getRepetitions() { return repetitions; }
    public void setRepetitions(int repetitions) { this.repetitions = repetitions; }
    public int getLapses() { return lapses; }
    public void setLapses(int lapses) { this.lapses = lapses; }
    public Instant getLastReviewedAt() { return lastReviewedAt; }
    public void setLastReviewedAt(Instant lastReviewedAt) { this.lastReviewedAt = lastReviewedAt; }
    public Instant getNextReviewAt() { return nextReviewAt; }
    public void setNextReviewAt(Instant nextReviewAt) { this.nextReviewAt = nextReviewAt; }
    public CardStateStatus getStatus() { return status; }
    public void setStatus(CardStateStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
