package com.example.englishquiz.repository;

import com.example.englishquiz.domain.AppUserEntity;
import com.example.englishquiz.domain.CardAnswerAliasEntity;
import com.example.englishquiz.domain.CardEntity;
import com.example.englishquiz.domain.DeckEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@org.springframework.boot.test.context.SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PersistenceIntegrationTest {

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
    private CardAnswerAliasRepository cardAnswerAliasRepository;

    @Test
    void shouldSaveAndLoadUserDeckCardAndAlias() {
        AppUserEntity user = new AppUserEntity();
        user.setId(UUID.randomUUID());
        user.setTelegramUserId(123456789L);
        user.setUsername("learner");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        appUserRepository.save(user);

        DeckEntity deck = new DeckEntity();
        deck.setId(UUID.randomUUID());
        deck.setOwner(user);
        deck.setName("Phrasal verbs");
        deck.setDescription("Core set");
        deck.setActive(true);
        deck.setCreatedAt(Instant.now());
        deck.setUpdatedAt(Instant.now());
        deckRepository.save(deck);

        CardEntity card = new CardEntity();
        card.setId(UUID.randomUUID());
        card.setDeck(deck);
        card.setRussianPrompt("откладывать");
        card.setEnglishAnswer("put off");
        card.setExampleSentence("I had to put off the meeting.");
        card.setExampleTranslation("Мне пришлось отложить встречу.");
        card.setPartOfSpeech("phrasal verb");
        card.setLevel("A2");
        card.setActive(true);
        card.setCreatedAt(Instant.now());
        card.setUpdatedAt(Instant.now());
        cardRepository.save(card);

        CardAnswerAliasEntity alias = new CardAnswerAliasEntity();
        alias.setId(UUID.randomUUID());
        alias.setCard(card);
        alias.setAlias("postpone");
        alias.setCreatedAt(Instant.now());
        cardAnswerAliasRepository.save(alias);

        CardEntity loadedCard = cardRepository.findById(card.getId()).orElseThrow();
        CardAnswerAliasEntity loadedAlias = cardAnswerAliasRepository.findById(alias.getId()).orElseThrow();

        assertEquals("откладывать", loadedCard.getRussianPrompt());
        assertEquals("postpone", loadedAlias.getAlias());
    }

    @Test
    void shouldRejectDuplicateAliasForSameCard() {
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

        CardAnswerAliasEntity aliasOne = new CardAnswerAliasEntity();
        aliasOne.setId(UUID.randomUUID());
        aliasOne.setCard(card);
        aliasOne.setAlias("discover");
        aliasOne.setCreatedAt(Instant.now());
        cardAnswerAliasRepository.saveAndFlush(aliasOne);

        CardAnswerAliasEntity aliasTwo = new CardAnswerAliasEntity();
        aliasTwo.setId(UUID.randomUUID());
        aliasTwo.setCard(card);
        aliasTwo.setAlias("discover");
        aliasTwo.setCreatedAt(Instant.now());

        assertThrows(DataIntegrityViolationException.class, () -> cardAnswerAliasRepository.saveAndFlush(aliasTwo));
    }
}
