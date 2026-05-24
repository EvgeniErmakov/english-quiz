package com.example.englishquiz.service.impl;

import com.example.englishquiz.domain.AppUserEntity;
import com.example.englishquiz.domain.CardAnswerAliasEntity;
import com.example.englishquiz.domain.CardEntity;
import com.example.englishquiz.domain.CardStateEntity;
import com.example.englishquiz.domain.ReviewAttemptEntity;
import com.example.englishquiz.dto.AnswerCheckRequestDto;
import com.example.englishquiz.dto.AnswerCheckResultDto;
import com.example.englishquiz.dto.ReviewResultDto;
import com.example.englishquiz.dto.SubmitReviewAnswerDto;
import com.example.englishquiz.enums.CardStateStatus;
import com.example.englishquiz.enums.ReviewRating;
import com.example.englishquiz.exception.BadRequestException;
import com.example.englishquiz.exception.NotFoundException;
import com.example.englishquiz.repository.AppUserRepository;
import com.example.englishquiz.repository.CardAnswerAliasRepository;
import com.example.englishquiz.repository.CardRepository;
import com.example.englishquiz.repository.CardStateRepository;
import com.example.englishquiz.repository.ReviewAttemptRepository;
import com.example.englishquiz.scheduler.ReviewScheduler;
import com.example.englishquiz.scheduler.dto.ReviewScheduleInput;
import com.example.englishquiz.scheduler.dto.ReviewScheduleResult;
import com.example.englishquiz.service.AnswerCheckingService;
import com.example.englishquiz.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final CardRepository cardRepository;
    private final CardAnswerAliasRepository cardAnswerAliasRepository;
    private final CardStateRepository cardStateRepository;
    private final ReviewAttemptRepository reviewAttemptRepository;
    private final AppUserRepository appUserRepository;
    private final AnswerCheckingService answerCheckingService;
    private final ReviewScheduler reviewScheduler;

    public ReviewServiceImpl(CardRepository cardRepository,
                             CardAnswerAliasRepository cardAnswerAliasRepository,
                             CardStateRepository cardStateRepository,
                             ReviewAttemptRepository reviewAttemptRepository,
                             AppUserRepository appUserRepository,
                             AnswerCheckingService answerCheckingService,
                             ReviewScheduler reviewScheduler) {
        this.cardRepository = cardRepository;
        this.cardAnswerAliasRepository = cardAnswerAliasRepository;
        this.cardStateRepository = cardStateRepository;
        this.reviewAttemptRepository = reviewAttemptRepository;
        this.appUserRepository = appUserRepository;
        this.answerCheckingService = answerCheckingService;
        this.reviewScheduler = reviewScheduler;
    }

    @Override
    @Transactional
    public ReviewResultDto submitAnswer(SubmitReviewAnswerDto submitReviewAnswerDto) {
        validateInput(submitReviewAnswerDto);
        Instant now = Instant.now();

        CardEntity cardEntity = cardRepository.findById(submitReviewAnswerDto.cardId())
                .filter(CardEntity::isActive)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        AppUserEntity userEntity = appUserRepository.findById(submitReviewAnswerDto.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<CardAnswerAliasEntity> aliasEntities = cardAnswerAliasRepository.findAllByCard(cardEntity);
        List<String> aliases = aliasEntities.stream().map(CardAnswerAliasEntity::getAlias).toList();

        AnswerCheckResultDto checkResultDto = answerCheckingService.checkAnswer(new AnswerCheckRequestDto(
                submitReviewAnswerDto.userAnswer(),
                cardEntity.getEnglishAnswer(),
                aliases));

        CardStateEntity cardStateEntity = cardStateRepository.findByUserIdAndCardId(userEntity.getId(), cardEntity.getId())
                .orElseGet(() -> createInitialState(userEntity, cardEntity, now));

        ReviewRating rating = checkResultDto.correct() ? ReviewRating.GOOD : ReviewRating.AGAIN;

        ReviewScheduleResult scheduleResult = reviewScheduler.schedule(new ReviewScheduleInput(
                cardStateEntity.getDifficulty(),
                cardStateEntity.getStability(),
                cardStateEntity.getRetrievability(),
                cardStateEntity.getRepetitions(),
                cardStateEntity.getLapses(),
                cardStateEntity.getStatus(),
                rating,
                now));

        applySchedule(cardStateEntity, scheduleResult, now);
        cardStateRepository.save(cardStateEntity);

        ReviewAttemptEntity attemptEntity = createAttempt(
                userEntity,
                cardEntity,
                cardStateEntity,
                submitReviewAnswerDto,
                checkResultDto,
                rating,
                now);
        reviewAttemptRepository.save(attemptEntity);

        LOGGER.info("Review attempt saved: userId={}, cardId={}, rating={}, correct={}",
                userEntity.getId(), cardEntity.getId(), rating, checkResultDto.correct());

        return new ReviewResultDto(
                cardEntity.getId(),
                checkResultDto.correct(),
                rating,
                checkResultDto.normalizedUserAnswer(),
                checkResultDto.normalizedExpectedAnswer(),
                cardStateEntity.getNextReviewAt(),
                checkResultDto.correct() ? "Correct answer" : "Incorrect answer");
    }

    private void validateInput(SubmitReviewAnswerDto submitReviewAnswerDto) {
        if (submitReviewAnswerDto.userId() == null) {
            throw new BadRequestException("userId is required");
        }
        if (submitReviewAnswerDto.cardId() == null) {
            throw new BadRequestException("cardId is required");
        }
        if (submitReviewAnswerDto.userAnswer() == null || submitReviewAnswerDto.userAnswer().isBlank()) {
            throw new BadRequestException("userAnswer is required");
        }
    }

    private CardStateEntity createInitialState(AppUserEntity userEntity, CardEntity cardEntity, Instant now) {
        CardStateEntity stateEntity = new CardStateEntity();
        stateEntity.setId(UUID.randomUUID());
        stateEntity.setUser(userEntity);
        stateEntity.setCard(cardEntity);
        stateEntity.setDifficulty(BigDecimal.ZERO.setScale(3));
        stateEntity.setStability(BigDecimal.ZERO.setScale(3));
        stateEntity.setRetrievability(null);
        stateEntity.setRepetitions(0);
        stateEntity.setLapses(0);
        stateEntity.setLastReviewedAt(null);
        stateEntity.setNextReviewAt(now);
        stateEntity.setStatus(CardStateStatus.NEW);
        stateEntity.setCreatedAt(now);
        stateEntity.setUpdatedAt(now);
        return stateEntity;
    }

    private void applySchedule(CardStateEntity cardStateEntity, ReviewScheduleResult scheduleResult, Instant now) {
        cardStateEntity.setDifficulty(scheduleResult.difficulty());
        cardStateEntity.setStability(scheduleResult.stability());
        cardStateEntity.setRetrievability(scheduleResult.retrievability());
        cardStateEntity.setRepetitions(scheduleResult.repetitions());
        cardStateEntity.setLapses(scheduleResult.lapses());
        cardStateEntity.setNextReviewAt(scheduleResult.nextReviewAt());
        cardStateEntity.setStatus(scheduleResult.status());
        cardStateEntity.setLastReviewedAt(now);
        cardStateEntity.setUpdatedAt(now);
    }

    private ReviewAttemptEntity createAttempt(AppUserEntity userEntity,
                                              CardEntity cardEntity,
                                              CardStateEntity cardStateEntity,
                                              SubmitReviewAnswerDto submitReviewAnswerDto,
                                              AnswerCheckResultDto checkResultDto,
                                              ReviewRating rating,
                                              Instant now) {
        ReviewAttemptEntity attemptEntity = new ReviewAttemptEntity();
        attemptEntity.setId(UUID.randomUUID());
        attemptEntity.setUser(userEntity);
        attemptEntity.setCard(cardEntity);
        attemptEntity.setCardState(cardStateEntity);
        attemptEntity.setUserAnswer(submitReviewAnswerDto.userAnswer());
        attemptEntity.setExpectedAnswer(cardEntity.getEnglishAnswer());
        attemptEntity.setNormalizedUserAnswer(checkResultDto.normalizedUserAnswer());
        attemptEntity.setNormalizedExpectedAnswer(checkResultDto.normalizedExpectedAnswer());
        attemptEntity.setCorrect(checkResultDto.correct());
        attemptEntity.setRating(rating);
        attemptEntity.setAiFeedback(null);
        attemptEntity.setResponseTimeMs(submitReviewAnswerDto.responseTimeMs());
        attemptEntity.setReviewedAt(now);
        return attemptEntity;
    }
}
