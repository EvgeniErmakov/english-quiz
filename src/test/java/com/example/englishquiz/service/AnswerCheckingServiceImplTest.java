package com.example.englishquiz.service;

import com.example.englishquiz.dto.AnswerCheckRequestDto;
import com.example.englishquiz.dto.AnswerCheckResultDto;
import com.example.englishquiz.enums.AnswerMatchType;
import com.example.englishquiz.service.impl.AnswerCheckingServiceImpl;
import com.example.englishquiz.service.impl.AnswerNormalizerServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnswerCheckingServiceImplTest {

    private final AnswerCheckingService answerCheckingService =
            new AnswerCheckingServiceImpl(new AnswerNormalizerServiceImpl());

    @Test
    void shouldMatchExpectedAnswerExactly() {
        AnswerCheckRequestDto requestDto = new AnswerCheckRequestDto("put off", "put off", List.of("postpone"));

        AnswerCheckResultDto resultDto = answerCheckingService.checkAnswer(requestDto);

        assertTrue(resultDto.correct());
        assertEquals(AnswerMatchType.EXPECTED, resultDto.matchType());
    }

    @Test
    void shouldMatchExpectedAnswerCaseInsensitively() {
        AnswerCheckRequestDto requestDto = new AnswerCheckRequestDto("Put Off", "put off", List.of());

        AnswerCheckResultDto resultDto = answerCheckingService.checkAnswer(requestDto);

        assertTrue(resultDto.correct());
        assertEquals("put off", resultDto.normalizedUserAnswer());
        assertEquals(AnswerMatchType.EXPECTED, resultDto.matchType());
    }

    @Test
    void shouldMatchExpectedAnswerWithRepeatedSpacesAndTrailingPunctuation() {
        AnswerCheckRequestDto requestDto = new AnswerCheckRequestDto("  put   off! ", "put off", List.of());

        AnswerCheckResultDto resultDto = answerCheckingService.checkAnswer(requestDto);

        assertTrue(resultDto.correct());
        assertEquals("put off", resultDto.normalizedUserAnswer());
        assertEquals(AnswerMatchType.EXPECTED, resultDto.matchType());
    }

    @Test
    void shouldMatchAliasWhenExpectedAnswerDoesNotMatch() {
        AnswerCheckRequestDto requestDto = new AnswerCheckRequestDto("  Postpone.", "put off", List.of("postpone"));

        AnswerCheckResultDto resultDto = answerCheckingService.checkAnswer(requestDto);

        assertTrue(resultDto.correct());
        assertEquals(AnswerMatchType.ALIAS, resultDto.matchType());
    }

    @Test
    void shouldReturnIncorrectWhenNoExpectedOrAliasMatch() {
        AnswerCheckRequestDto requestDto = new AnswerCheckRequestDto("give up", "put off", List.of("postpone"));

        AnswerCheckResultDto resultDto = answerCheckingService.checkAnswer(requestDto);

        assertFalse(resultDto.correct());
        assertEquals(AnswerMatchType.NONE, resultDto.matchType());
    }
}
