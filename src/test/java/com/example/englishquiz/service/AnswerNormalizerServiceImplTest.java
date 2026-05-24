package com.example.englishquiz.service;

import com.example.englishquiz.service.impl.AnswerNormalizerServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnswerNormalizerServiceImplTest {

    private final AnswerNormalizerServiceImpl answerNormalizerService = new AnswerNormalizerServiceImpl();

    @Test
    void shouldNormalizeSpacesCaseAndTrailingPunctuation() {
        String normalizedAnswer = answerNormalizerService.normalize("  Put   Off! ");

        assertEquals("put off", normalizedAnswer);
    }

    @Test
    void shouldTrimLeadingAndTrailingSpaces() {
        String normalizedAnswer = answerNormalizerService.normalize("   reliable   ");

        assertEquals("reliable", normalizedAnswer);
    }

    @Test
    void shouldCollapseRepeatedSpaces() {
        String normalizedAnswer = answerNormalizerService.normalize("find    out");

        assertEquals("find out", normalizedAnswer);
    }

    @Test
    void shouldHandleNullAsEmptyString() {
        String normalizedAnswer = answerNormalizerService.normalize(null);

        assertEquals("", normalizedAnswer);
    }
}
