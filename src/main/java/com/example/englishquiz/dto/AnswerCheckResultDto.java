package com.example.englishquiz.dto;

import com.example.englishquiz.enums.AnswerMatchType;

public record AnswerCheckResultDto(
        String originalUserAnswer,
        String normalizedUserAnswer,
        String normalizedExpectedAnswer,
        boolean correct,
        AnswerMatchType matchType) {
}
