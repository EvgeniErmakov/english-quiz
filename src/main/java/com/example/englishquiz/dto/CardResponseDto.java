package com.example.englishquiz.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CardResponseDto(
        UUID id,
        UUID deckId,
        String russianPrompt,
        String englishAnswer,
        String exampleSentence,
        String exampleTranslation,
        String partOfSpeech,
        String level,
        boolean isActive,
        List<CardAliasResponseDto> aliases,
        Instant createdAt,
        Instant updatedAt) {
}
