package com.example.englishquiz.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cards")
public class CardEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private DeckEntity deck;

    @Column(name = "russian_prompt", nullable = false)
    private String russianPrompt;

    @Column(name = "english_answer", nullable = false)
    private String englishAnswer;

    @Column(name = "example_sentence")
    private String exampleSentence;

    @Column(name = "example_translation")
    private String exampleTranslation;

    @Column(name = "part_of_speech")
    private String partOfSpeech;

    @Column(name = "level")
    private String level;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public DeckEntity getDeck() { return deck; }

    public void setDeck(DeckEntity deck) { this.deck = deck; }

    public String getRussianPrompt() { return russianPrompt; }

    public void setRussianPrompt(String russianPrompt) { this.russianPrompt = russianPrompt; }

    public String getEnglishAnswer() { return englishAnswer; }

    public void setEnglishAnswer(String englishAnswer) { this.englishAnswer = englishAnswer; }

    public String getExampleSentence() { return exampleSentence; }

    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }

    public String getExampleTranslation() { return exampleTranslation; }

    public void setExampleTranslation(String exampleTranslation) { this.exampleTranslation = exampleTranslation; }

    public String getPartOfSpeech() { return partOfSpeech; }

    public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }

    public String getLevel() { return level; }

    public void setLevel(String level) { this.level = level; }

    public boolean isActive() { return isActive; }

    public void setActive(boolean active) { isActive = active; }

    public Instant getCreatedAt() { return createdAt; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
