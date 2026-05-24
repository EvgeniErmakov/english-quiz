package com.example.englishquiz.service;

import com.example.englishquiz.domain.AppUserEntity;
import com.example.englishquiz.domain.CardEntity;
import com.example.englishquiz.domain.CardStateEntity;
import com.example.englishquiz.domain.DeckEntity;
import com.example.englishquiz.enums.CardStateStatus;
import com.example.englishquiz.repository.CardStateRepository;
import com.example.englishquiz.service.impl.AgentReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentReviewServiceImplTest {

    @Mock
    private CardStateRepository cardStateRepository;

    @Test
    void shouldReturnFirstActiveDueCard() {
        AgentReviewService agentReviewService = new AgentReviewServiceImpl(cardStateRepository);
        UUID userId = UUID.randomUUID();

        CardStateEntity inactiveState = createState(false, Instant.now().minusSeconds(120));
        CardStateEntity activeState = createState(true, Instant.now().minusSeconds(60));

        Page<CardStateEntity> page = new PageImpl<>(List.of(inactiveState, activeState));
        when(cardStateRepository.findByUserIdAndStatusInAndNextReviewAtLessThanEqual(any(), any(), any(), any()))
                .thenReturn(page);

        Optional<?> result = agentReviewService.findNextDueCard(userId);

        assertTrue(result.isPresent());
        assertEquals(activeState.getCard().getId(), ((com.example.englishquiz.dto.AgentDueCardResponseDto) result.get()).cardId());
    }

    private CardStateEntity createState(boolean cardActive, Instant nextReviewAt) {
        AppUserEntity user = new AppUserEntity();
        user.setId(UUID.randomUUID());

        DeckEntity deck = new DeckEntity();
        deck.setId(UUID.randomUUID());

        CardEntity card = new CardEntity();
        card.setId(UUID.randomUUID());
        card.setDeck(deck);
        card.setRussianPrompt("выяснить");
        card.setActive(cardActive);

        CardStateEntity state = new CardStateEntity();
        state.setId(UUID.randomUUID());
        state.setUser(user);
        state.setCard(card);
        state.setDifficulty(BigDecimal.ZERO);
        state.setStability(BigDecimal.ZERO);
        state.setRepetitions(0);
        state.setLapses(0);
        state.setStatus(CardStateStatus.REVIEW);
        state.setNextReviewAt(nextReviewAt);
        state.setCreatedAt(Instant.now());
        state.setUpdatedAt(Instant.now());
        return state;
    }
}
