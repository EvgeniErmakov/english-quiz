package com.example.englishquiz.dto;

import java.util.UUID;

public record SubmitReviewAnswerDto(
        UUID userId,
        UUID cardId,
        String userAnswer,
        Integer responseTimeMs) {
}
