package com.example.englishquiz.service;

import com.example.englishquiz.domain.AppUserEntity;
import com.example.englishquiz.domain.CardAnswerAliasEntity;
import com.example.englishquiz.domain.CardEntity;
import com.example.englishquiz.domain.CardStateEntity;
import com.example.englishquiz.domain.DeckEntity;
import com.example.englishquiz.domain.ReviewAttemptEntity;
import com.example.englishquiz.dto.AnswerCheckResultDto;
import com.example.englishquiz.dto.ReviewResultDto;
import com.example.englishquiz.dto.SubmitReviewAnswerDto;
import com.example.englishquiz.enums.AnswerMatchType;
import com.example.englishquiz.enums.CardStateStatus;
import com.example.englishquiz.enums.ReviewRating;
import com.example.englishquiz.exception.NotFoundException;
import com.example.englishquiz.repository.AppUserRepository;
import com.example.englishquiz.repository.CardAnswerAliasRepository;
import com.example.englishquiz.repository.CardRepository;
import com.example.englishquiz.repository.CardStateRepository;
import com.example.englishquiz.repository.ReviewAttemptRepository;
import com.example.englishquiz.scheduler.ReviewScheduler;
import com.example.englishquiz.scheduler.dto.ReviewScheduleInput;
import com.example.englishquiz.scheduler.dto.ReviewScheduleResult;
import com.example.englishquiz.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardAnswerAliasRepository cardAnswerAliasRepository;
    @Mock
    private CardStateRepository cardStateRepository;
    @Mock
    private ReviewAttemptRepository reviewAttemptRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private AnswerCheckingService answerCheckingService;
    @Mock
    private ReviewScheduler reviewScheduler;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(
                cardRepository,
                cardAnswerAliasRepository,
                cardStateRepository,
                reviewAttemptRepository,
                appUserRepository,
                answerCheckingService,
                reviewScheduler);
    }

    @Test
    void correctAnswerShouldCreateAttemptAndScheduleGood() {
        TestData data = baseData();
        when(answerCheckingService.checkAnswer(any())).thenReturn(new AnswerCheckResultDto(
                "put off", "put off", "put off", true, AnswerMatchType.EXPECTED));
        when(cardStateRepository.findByUserIdAndCardId(data.userId, data.cardId)).thenReturn(Optional.of(data.state));
        when(reviewScheduler.schedule(any())).thenReturn(schedule(ReviewRating.GOOD));

        ReviewResultDto resultDto = reviewService.submitAnswer(new SubmitReviewAnswerDto(
                data.userId, data.cardId, "put off", 1200));

        assertEquals(ReviewRating.GOOD, resultDto.rating());
        assertTrue(resultDto.isCorrect());
        ArgumentCaptor<ReviewScheduleInput> captor = ArgumentCaptor.forClass(ReviewScheduleInput.class);
        verify(reviewScheduler).schedule(captor.capture());
        assertEquals(ReviewRating.GOOD, captor.getValue().rating());
        verify(reviewAttemptRepository).save(any(ReviewAttemptEntity.class));
    }

    @Test
    void aliasAnswerShouldCreateAttemptAndScheduleGood() {
        TestData data = baseData();
        when(answerCheckingService.checkAnswer(any())).thenReturn(new AnswerCheckResultDto(
                "postpone", "postpone", "put off", true, AnswerMatchType.ALIAS));
        when(cardStateRepository.findByUserIdAndCardId(data.userId, data.cardId)).thenReturn(Optional.of(data.state));
        when(reviewScheduler.schedule(any())).thenReturn(schedule(ReviewRating.GOOD));

        ReviewResultDto resultDto = reviewService.submitAnswer(new SubmitReviewAnswerDto(
                data.userId, data.cardId, "postpone", 1500));

        assertEquals(ReviewRating.GOOD, resultDto.rating());
        assertTrue(resultDto.isCorrect());
    }

    @Test
    void incorrectAnswerShouldCreateAttemptAndScheduleAgain() {
        TestData data = baseData();
        when(answerCheckingService.checkAnswer(any())).thenReturn(new AnswerCheckResultDto(
                "give up", "give up", "put off", false, AnswerMatchType.NONE));
        when(cardStateRepository.findByUserIdAndCardId(data.userId, data.cardId)).thenReturn(Optional.of(data.state));
        when(reviewScheduler.schedule(any())).thenReturn(schedule(ReviewRating.AGAIN));

        ReviewResultDto resultDto = reviewService.submitAnswer(new SubmitReviewAnswerDto(
                data.userId, data.cardId, "give up", null));

        assertEquals(ReviewRating.AGAIN, resultDto.rating());
        assertFalse(resultDto.isCorrect());
        verify(reviewAttemptRepository).save(any(ReviewAttemptEntity.class));
    }

    @Test
    void missingStateShouldCreateInitialState() {
        TestData data = baseData();
        when(answerCheckingService.checkAnswer(any())).thenReturn(new AnswerCheckResultDto(
                "put off", "put off", "put off", true, AnswerMatchType.EXPECTED));
        when(cardStateRepository.findByUserIdAndCardId(data.userId, data.cardId)).thenReturn(Optional.empty());
        when(reviewScheduler.schedule(any())).thenReturn(schedule(ReviewRating.GOOD));

        reviewService.submitAnswer(new SubmitReviewAnswerDto(data.userId, data.cardId, "put off", 500));

        ArgumentCaptor<CardStateEntity> stateCaptor = ArgumentCaptor.forClass(CardStateEntity.class);
        verify(cardStateRepository).save(stateCaptor.capture());
        assertEquals(CardStateStatus.REVIEW, stateCaptor.getValue().getStatus());
        assertEquals(1, stateCaptor.getValue().getRepetitions());
    }

    @Test
    void existingStateShouldBeUpdated() {
        TestData data = baseData();
        data.state.setRepetitions(2);
        when(answerCheckingService.checkAnswer(any())).thenReturn(new AnswerCheckResultDto(
                "put off", "put off", "put off", true, AnswerMatchType.EXPECTED));
        when(cardStateRepository.findByUserIdAndCardId(data.userId, data.cardId)).thenReturn(Optional.of(data.state));
        when(reviewScheduler.schedule(any())).thenReturn(new ReviewScheduleResult(
                BigDecimal.ONE,
                BigDecimal.valueOf(2),
                null,
                3,
                0,
                Instant.now().plusSeconds(3600),
                CardStateStatus.REVIEW));

        reviewService.submitAnswer(new SubmitReviewAnswerDto(data.userId, data.cardId, "put off", 800));

        assertEquals(3, data.state.getRepetitions());
        verify(cardStateRepository).save(data.state);
    }

    @Test
    void missingCardShouldThrowNotFound() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reviewService.submitAnswer(new SubmitReviewAnswerDto(
                userId, cardId, "put off", 1000)));
    }

    private ReviewScheduleResult schedule(ReviewRating rating) {
        if (rating == ReviewRating.AGAIN) {
            return new ReviewScheduleResult(BigDecimal.ZERO, BigDecimal.ZERO, null, 0, 1,
                    Instant.now().plusSeconds(900), CardStateStatus.LEARNING);
        }
        return new ReviewScheduleResult(BigDecimal.ZERO, BigDecimal.ONE, null, 1, 0,
                Instant.now().plusSeconds(86400), CardStateStatus.REVIEW);
    }

    private TestData baseData() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        AppUserEntity user = new AppUserEntity();
        user.setId(userId);

        DeckEntity deck = new DeckEntity();
        deck.setId(UUID.randomUUID());

        CardEntity card = new CardEntity();
        card.setId(cardId);
        card.setDeck(deck);
        card.setEnglishAnswer("put off");
        card.setActive(true);

        CardAnswerAliasEntity alias = new CardAnswerAliasEntity();
        alias.setId(UUID.randomUUID());
        alias.setCard(card);
        alias.setAlias("postpone");

        CardStateEntity state = new CardStateEntity();
        state.setId(UUID.randomUUID());
        state.setUser(user);
        state.setCard(card);
        state.setDifficulty(BigDecimal.ZERO);
        state.setStability(BigDecimal.ZERO);
        state.setRepetitions(0);
        state.setLapses(0);
        state.setNextReviewAt(Instant.now());
        state.setStatus(CardStateStatus.NEW);
        state.setCreatedAt(Instant.now());
        state.setUpdatedAt(Instant.now());

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardAnswerAliasRepository.findAllByCard(card)).thenReturn(List.of(alias));
        when(cardStateRepository.save(any(CardStateEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        return new TestData(userId, cardId, state);
    }

    private record TestData(UUID userId, UUID cardId, CardStateEntity state) {
    }
}
