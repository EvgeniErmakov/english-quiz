package com.example.englishquiz.dto;

import java.util.List;

public record AnswerCheckRequestDto(
        String userAnswer,
        String expectedAnswer,
        List<String> answerAliases) {
}
