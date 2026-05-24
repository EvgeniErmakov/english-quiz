package com.example.englishquiz.service;

import com.example.englishquiz.domain.CardEntity;
import com.example.englishquiz.domain.DeckEntity;
import com.example.englishquiz.dto.CreateCardRequestDto;
import com.example.englishquiz.dto.UpdateCardRequestDto;
import com.example.englishquiz.exception.BadRequestException;
import com.example.englishquiz.mapper.CardMapper;
import com.example.englishquiz.repository.CardAnswerAliasRepository;
import com.example.englishquiz.repository.CardRepository;
import com.example.englishquiz.repository.DeckRepository;
import com.example.englishquiz.service.impl.AnswerNormalizerServiceImpl;
import com.example.englishquiz.service.impl.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private CardAnswerAliasRepository cardAnswerAliasRepository;

    @Mock
    private CardMapper cardMapper;

    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardServiceImpl(
                cardRepository,
                deckRepository,
                cardAnswerAliasRepository,
                cardMapper,
                new AnswerNormalizerServiceImpl());
    }

    @Test
    void shouldRejectDuplicateAliasesAfterNormalizationOnCreate() {
        DeckEntity deckEntity = new DeckEntity();
        UUID deckId = UUID.randomUUID();
        deckEntity.setId(deckId);

        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deckEntity));
        when(cardRepository.save(any(CardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCardRequestDto requestDto = new CreateCardRequestDto(
                deckId,
                "откладывать",
                "put off",
                null,
                null,
                null,
                null,
                List.of("Postpone", " postpone! "));

        assertThrows(BadRequestException.class, () -> cardService.create(requestDto));
    }

    @Test
    void shouldRejectDuplicateAliasesAfterNormalizationOnUpdate() {
        UUID cardId = UUID.randomUUID();
        DeckEntity deckEntity = new DeckEntity();
        deckEntity.setId(UUID.randomUUID());

        CardEntity cardEntity = new CardEntity();
        cardEntity.setId(cardId);
        cardEntity.setDeck(deckEntity);
        cardEntity.setActive(true);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(cardEntity));
        when(cardRepository.save(any(CardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCardRequestDto requestDto = new UpdateCardRequestDto(
                "откладывать",
                "put off",
                null,
                null,
                null,
                null,
                List.of("postpone", "Postpone."));

        assertThrows(BadRequestException.class, () -> cardService.update(cardId, requestDto));
    }
}
