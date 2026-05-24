package com.example.englishquiz.dto;

import java.time.Instant;
import java.util.UUID;

public record AgentDueCardResponseDto(
        UUID cardId,
        UUID cardStateId,
        String russianPrompt,
        String exampleTranslation,
        String partOfSpeech,
        String level,
        Instant nextReviewAt) {
}
