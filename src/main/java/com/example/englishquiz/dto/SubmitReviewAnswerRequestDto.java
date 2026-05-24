package com.example.englishquiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SubmitReviewAnswerRequestDto(
        @NotNull UUID userId,
        @NotNull UUID cardId,
        @NotBlank @Size(max = 1000) String userAnswer,
        @PositiveOrZero Integer responseTimeMs) {
}
