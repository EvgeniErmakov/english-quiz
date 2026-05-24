package com.example.englishquiz.service.impl;

import com.example.englishquiz.dto.AnswerCheckRequestDto;
import com.example.englishquiz.dto.AnswerCheckResultDto;
import com.example.englishquiz.enums.AnswerMatchType;
import com.example.englishquiz.service.AnswerCheckingService;
import com.example.englishquiz.service.AnswerNormalizerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerCheckingServiceImpl implements AnswerCheckingService {

    private final AnswerNormalizerService answerNormalizerService;

    public AnswerCheckingServiceImpl(AnswerNormalizerService answerNormalizerService) {
        this.answerNormalizerService = answerNormalizerService;
    }

    @Override
    public AnswerCheckResultDto checkAnswer(AnswerCheckRequestDto requestDto) {
        String normalizedUserAnswer = answerNormalizerService.normalize(requestDto.userAnswer());
        String normalizedExpectedAnswer = answerNormalizerService.normalize(requestDto.expectedAnswer());

        if (normalizedUserAnswer.equals(normalizedExpectedAnswer)) {
            return new AnswerCheckResultDto(
                    requestDto.userAnswer(),
                    normalizedUserAnswer,
                    normalizedExpectedAnswer,
                    true,
                    AnswerMatchType.EXPECTED);
        }

        List<String> aliases = requestDto.answerAliases() == null ? List.of() : requestDto.answerAliases();

        for (String alias : aliases) {
            String normalizedAlias = answerNormalizerService.normalize(alias);
            if (normalizedUserAnswer.equals(normalizedAlias)) {
                return new AnswerCheckResultDto(
                        requestDto.userAnswer(),
                        normalizedUserAnswer,
                        normalizedExpectedAnswer,
                        true,
                        AnswerMatchType.ALIAS);
            }
        }

        return new AnswerCheckResultDto(
                requestDto.userAnswer(),
                normalizedUserAnswer,
                normalizedExpectedAnswer,
                false,
                AnswerMatchType.NONE);
    }
}
