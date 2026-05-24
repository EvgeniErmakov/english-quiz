package com.example.englishquiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateCardRequestDto(
        @NotNull UUID deckId,
        @NotBlank @Size(max = 500) String russianPrompt,
        @NotBlank @Size(max = 500) String englishAnswer,
        @Size(max = 1000) String exampleSentence,
        @Size(max = 1000) String exampleTranslation,
        @Size(max = 50) String partOfSpeech,
        @Size(max = 20) String level,
        List<@NotBlank @Size(max = 500) String> aliases) {
}
