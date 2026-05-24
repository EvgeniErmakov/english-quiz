package com.example.englishquiz.dto;

import com.example.englishquiz.enums.ReviewRating;

import java.time.Instant;
import java.util.UUID;

public record SubmitReviewAnswerResponseDto(
        UUID cardId,
        boolean isCorrect,
        ReviewRating rating,
        String normalizedUserAnswer,
        String normalizedExpectedAnswer,
        Instant nextReviewAt,
        String message) {
}
