package com.example.englishquiz.scheduler;

import com.example.englishquiz.enums.CardStateStatus;
import com.example.englishquiz.enums.ReviewRating;
import com.example.englishquiz.scheduler.dto.ReviewScheduleInput;
import com.example.englishquiz.scheduler.dto.ReviewScheduleResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class BasicReviewScheduler implements ReviewScheduler {

    @Override
    public ReviewScheduleResult schedule(ReviewScheduleInput input) {
        BigDecimal currentDifficulty = nullableDecimal(input.difficulty());
        BigDecimal currentStability = nullableDecimal(input.stability());
        BigDecimal currentRetrievability = input.retrievability();
        int currentRepetitions = input.repetitions();
        int currentLapses = input.lapses();
        Instant now = input.now();

        if (input.rating() == ReviewRating.AGAIN) {
            return new ReviewScheduleResult(
                    increaseDifficulty(currentDifficulty, BigDecimal.valueOf(0.30)),
                    decreaseStability(currentStability),
                    currentRetrievability,
                    currentRepetitions,
                    currentLapses + 1,
                    now.plus(15, ChronoUnit.MINUTES),
                    CardStateStatus.LEARNING);
        }

        if (input.rating() == ReviewRating.HARD) {
            return new ReviewScheduleResult(
                    increaseDifficulty(currentDifficulty, BigDecimal.valueOf(0.10)),
                    increaseStability(currentStability, BigDecimal.valueOf(0.30)),
                    currentRetrievability,
                    currentRepetitions + 1,
                    currentLapses,
                    now.plus(1, ChronoUnit.DAYS),
                    CardStateStatus.REVIEW);
        }

        if (input.rating() == ReviewRating.GOOD) {
            return scheduleSuccessful(input, now, currentDifficulty, currentStability, currentRetrievability, 3, 1.4);
        }

        return scheduleSuccessful(input, now, currentDifficulty, currentStability, currentRetrievability, 7, 1.8);
    }

    private ReviewScheduleResult scheduleSuccessful(ReviewScheduleInput input,
                                                    Instant now,
                                                    BigDecimal currentDifficulty,
                                                    BigDecimal currentStability,
                                                    BigDecimal currentRetrievability,
                                                    int firstIntervalDays,
                                                    double growthFactor) {
        int nextRepetitions = input.repetitions() + 1;
        BigDecimal nextStability = increaseStability(currentStability, BigDecimal.valueOf(growthFactor));

        long intervalDays = nextRepetitions == 1
                ? firstIntervalDays
                : Math.max(firstIntervalDays, Math.round(nextStability.doubleValue() + nextRepetitions * growthFactor));

        return new ReviewScheduleResult(
                decreaseDifficulty(currentDifficulty),
                nextStability,
                currentRetrievability,
                nextRepetitions,
                input.lapses(),
                now.plus(intervalDays, ChronoUnit.DAYS),
                CardStateStatus.REVIEW);
    }

    private BigDecimal nullableDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP) : value;
    }

    private BigDecimal increaseDifficulty(BigDecimal currentDifficulty, BigDecimal delta) {
        return currentDifficulty.add(delta).setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal decreaseDifficulty(BigDecimal currentDifficulty) {
        BigDecimal decreased = currentDifficulty.subtract(BigDecimal.valueOf(0.05));
        if (decreased.signum() < 0) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }
        return decreased.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal increaseStability(BigDecimal currentStability, BigDecimal delta) {
        return currentStability.add(delta).setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal decreaseStability(BigDecimal currentStability) {
        BigDecimal decreased = currentStability.multiply(BigDecimal.valueOf(0.5));
        return decreased.setScale(3, RoundingMode.HALF_UP);
    }
}
