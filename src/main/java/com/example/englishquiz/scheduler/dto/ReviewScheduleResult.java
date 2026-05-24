package com.example.englishquiz.scheduler.dto;

import com.example.englishquiz.enums.CardStateStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ReviewScheduleResult(
        BigDecimal difficulty,
        BigDecimal stability,
        BigDecimal retrievability,
        int repetitions,
        int lapses,
        Instant nextReviewAt,
        CardStateStatus status) {
}
