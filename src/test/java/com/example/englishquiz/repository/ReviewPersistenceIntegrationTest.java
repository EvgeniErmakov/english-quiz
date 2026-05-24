package com.example.englishquiz.repository;

import com.example.englishquiz.domain.AppUserEntity;
import com.example.englishquiz.domain.CardEntity;
import com.example.englishquiz.domain.CardStateEntity;
import com.example.englishquiz.domain.DeckEntity;
import com.example.englishquiz.domain.ReviewAttemptEntity;
import com.example.englishquiz.enums.CardStateStatus;
import com.example.englishquiz.enums.ReviewRating;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@org.springframework.boot.test.context.SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class ReviewPersistenceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> true);
    }

    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private DeckRepository deckRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardStateRepository cardStateRepository;
    @Autowired
    private ReviewAttemptRepository reviewAttemptRepository;

    @Test
    void shouldSaveStateAndAttemptAndFindDueStates() {
        AppUserEntity user = new AppUserEntity();
        user.setId(UUID.randomUUID());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        appUserRepository.save(user);

        DeckEntity deck = new DeckEntity();
        deck.setId(UUID.randomUUID());
        deck.setOwner(user);
        deck.setName("Deck");
        deck.setActive(true);
        deck.setCreatedAt(Instant.now());
        deck.setUpdatedAt(Instant.now());
        deckRepository.save(deck);

        CardEntity card = new CardEntity();
        card.setId(UUID.randomUUID());
        card.setDeck(deck);
        card.setRussianPrompt("выяснить");
        card.setEnglishAnswer("find out");
        card.setActive(true);
        card.setCreatedAt(Instant.now());
        card.setUpdatedAt(Instant.now());
        cardRepository.save(card);

        CardStateEntity state = new CardStateEntity();
        state.setId(UUID.randomUUID());
        state.setUser(user);
        state.setCard(card);
        state.setDifficulty(BigDecimal.ZERO);
        state.setStability(BigDecimal.ZERO);
        state.setRetrievability(null);
        state.setRepetitions(0);
        state.setLapses(0);
        state.setNextReviewAt(Instant.now().minusSeconds(60));
        state.setStatus(CardStateStatus.LEARNING);
        state.setCreatedAt(Instant.now());
        state.setUpdatedAt(Instant.now());
        cardStateRepository.save(state);

        ReviewAttemptEntity attempt = new ReviewAttemptEntity();
        attempt.setId(UUID.randomUUID());
        attempt.setUser(user);
        attempt.setCard(card);
        attempt.setCardState(state);
        attempt.setUserAnswer("find out");
        attempt.setExpectedAnswer("find out");
        attempt.setNormalizedUserAnswer("find out");
        attempt.setNormalizedExpectedAnswer("find out");
        attempt.setCorrect(true);
        attempt.setRating(ReviewRating.GOOD);
        attempt.setReviewedAt(Instant.now());
        reviewAttemptRepository.save(attempt);

        Page<CardStateEntity> due = cardStateRepository.findByUserIdAndStatusInAndNextReviewAtLessThanEqual(
                user.getId(),
                List.of(CardStateStatus.LEARNING, CardStateStatus.REVIEW),
                Instant.now(),
                PageRequest.of(0, 10));

        assertEquals(1, due.getTotalElements());
    }

    @Test
    void shouldEnforceUniqueUserAndCardInCardState() {
        AppUserEntity user = new AppUserEntity();
        user.setId(UUID.randomUUID());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        appUserRepository.save(user);

        DeckEntity deck = new DeckEntity();
        deck.setId(UUID.randomUUID());
        deck.setOwner(user);
        deck.setName("Deck");
        deck.setActive(true);
        deck.setCreatedAt(Instant.now());
        deck.setUpdatedAt(Instant.now());
        deckRepository.save(deck);

        CardEntity card = new CardEntity();
        card.setId(UUID.randomUUID());
        card.setDeck(deck);
        card.setRussianPrompt("надежный");
        card.setEnglishAnswer("reliable");
        card.setActive(true);
        card.setCreatedAt(Instant.now());
        card.setUpdatedAt(Instant.now());
        cardRepository.save(card);

        CardStateEntity first = new CardStateEntity();
        first.setId(UUID.randomUUID());
        first.setUser(user);
        first.setCard(card);
        first.setDifficulty(BigDecimal.ZERO);
        first.setStability(BigDecimal.ZERO);
        first.setRepetitions(0);
        first.setLapses(0);
        first.setNextReviewAt(Instant.now());
        first.setStatus(CardStateStatus.NEW);
        first.setCreatedAt(Instant.now());
        first.setUpdatedAt(Instant.now());
        cardStateRepository.saveAndFlush(first);

        CardStateEntity duplicate = new CardStateEntity();
        duplicate.setId(UUID.randomUUID());
        duplicate.setUser(user);
        duplicate.setCard(card);
        duplicate.setDifficulty(BigDecimal.ZERO);
        duplicate.setStability(BigDecimal.ZERO);
        duplicate.setRepetitions(0);
        duplicate.setLapses(0);
        duplicate.setNextReviewAt(Instant.now());
        duplicate.setStatus(CardStateStatus.NEW);
        duplicate.setCreatedAt(Instant.now());
        duplicate.setUpdatedAt(Instant.now());

        assertThrows(DataIntegrityViolationException.class, () -> cardStateRepository.saveAndFlush(duplicate));
    }
}
