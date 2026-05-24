package com.example.englishquiz.scheduler.dto;

import com.example.englishquiz.enums.CardStateStatus;
import com.example.englishquiz.enums.ReviewRating;

import java.math.BigDecimal;
import java.time.Instant;

public record ReviewScheduleInput(
        BigDecimal difficulty,
        BigDecimal stability,
        BigDecimal retrievability,
        int repetitions,
        int lapses,
        CardStateStatus status,
        ReviewRating rating,
        Instant now) {
}
