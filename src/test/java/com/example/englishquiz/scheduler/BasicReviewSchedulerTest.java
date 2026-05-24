package com.example.englishquiz.scheduler;

import com.example.englishquiz.enums.CardStateStatus;
import com.example.englishquiz.enums.ReviewRating;
import com.example.englishquiz.scheduler.dto.ReviewScheduleInput;
import com.example.englishquiz.scheduler.dto.ReviewScheduleResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicReviewSchedulerTest {

    private final BasicReviewScheduler scheduler = new BasicReviewScheduler();

    @Test
    void shouldScheduleAgainInFifteenMinutesAndIncrementLapses() {
        Instant now = Instant.parse("2026-05-24T12:00:00Z");
        ReviewScheduleInput input = new ReviewScheduleInput(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                0,
                1,
                CardStateStatus.NEW,
                ReviewRating.AGAIN,
                now);

        ReviewScheduleResult result = scheduler.schedule(input);

        assertEquals(now.plusSeconds(15 * 60), result.nextReviewAt());
        assertEquals(2, result.lapses());
        assertEquals(CardStateStatus.LEARNING, result.status());
    }

    @Test
    void shouldScheduleHardInOneDay() {
        Instant now = Instant.parse("2026-05-24T12:00:00Z");
        ReviewScheduleResult result = scheduler.schedule(new ReviewScheduleInput(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                0,
                0,
                CardStateStatus.LEARNING,
                ReviewRating.HARD,
                now));

        assertEquals(now.plusSeconds(24 * 60 * 60), result.nextReviewAt());
        assertEquals(1, result.repetitions());
    }

    @Test
    void shouldScheduleGoodLaterThanHard() {
        Instant now = Instant.parse("2026-05-24T12:00:00Z");
        ReviewScheduleResult hard = scheduler.schedule(new ReviewScheduleInput(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                0,
                0,
                CardStateStatus.NEW,
                ReviewRating.HARD,
                now));
        ReviewScheduleResult good = scheduler.schedule(new ReviewScheduleInput(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                0,
                0,
                CardStateStatus.NEW,
                ReviewRating.GOOD,
                now));

        assertTrue(good.nextReviewAt().isAfter(hard.nextReviewAt()));
        assertEquals(1, good.repetitions());
    }

    @Test
    void shouldScheduleEasyLaterThanGood() {
        Instant now = Instant.parse("2026-05-24T12:00:00Z");
        ReviewScheduleResult good = scheduler.schedule(new ReviewScheduleInput(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                0,
                0,
                CardStateStatus.NEW,
                ReviewRating.GOOD,
                now));
        ReviewScheduleResult easy = scheduler.schedule(new ReviewScheduleInput(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                0,
                0,
                CardStateStatus.NEW,
                ReviewRating.EASY,
                now));

        assertTrue(easy.nextReviewAt().isAfter(good.nextReviewAt()));
        assertEquals(1, easy.repetitions());
    }

    @Test
    void successfulRatingsShouldIncrementRepetitions() {
        Instant now = Instant.parse("2026-05-24T12:00:00Z");
        ReviewScheduleResult good = scheduler.schedule(new ReviewScheduleInput(
                BigDecimal.ZERO,
                BigDecimal.ONE,
                null,
                2,
                0,
                CardStateStatus.REVIEW,
                ReviewRating.GOOD,
                now));
        ReviewScheduleResult easy = scheduler.schedule(new ReviewScheduleInput(
                BigDecimal.ZERO,
                BigDecimal.ONE,
                null,
                2,
                0,
                CardStateStatus.REVIEW,
                ReviewRating.EASY,
                now));

        assertEquals(3, good.repetitions());
        assertEquals(3, easy.repetitions());
    }
}
