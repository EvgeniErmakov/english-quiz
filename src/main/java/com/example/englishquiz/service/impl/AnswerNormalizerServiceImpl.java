package com.example.englishquiz.service.impl;

import com.example.englishquiz.service.AnswerNormalizerService;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AnswerNormalizerServiceImpl implements AnswerNormalizerService {

    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s+");
    private static final Pattern TRAILING_PUNCTUATION_PATTERN = Pattern.compile("[.!?,;:]+$");

    @Override
    public String normalize(String answer) {
        if (answer == null) {
            return "";
        }

        String trimmedAnswer = answer.trim();
        String collapsedSpacesAnswer = MULTIPLE_SPACES_PATTERN.matcher(trimmedAnswer).replaceAll(" ");
        String withoutTrailingPunctuation = TRAILING_PUNCTUATION_PATTERN.matcher(collapsedSpacesAnswer).replaceAll("");

        return withoutTrailingPunctuation.toLowerCase();
    }
}
