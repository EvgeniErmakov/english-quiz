package com.example.englishquiz.service.impl;

import com.example.englishquiz.domain.CardEntity;
import com.example.englishquiz.domain.CardStateEntity;
import com.example.englishquiz.dto.AgentDueCardResponseDto;
import com.example.englishquiz.enums.CardStateStatus;
import com.example.englishquiz.repository.CardStateRepository;
import com.example.englishquiz.service.AgentReviewService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

@Service
public class AgentReviewServiceImpl implements AgentReviewService {

    private final CardStateRepository cardStateRepository;

    public AgentReviewServiceImpl(CardStateRepository cardStateRepository) {
        this.cardStateRepository = cardStateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AgentDueCardResponseDto> findNextDueCard(UUID userId) {
        return cardStateRepository.findByUserIdAndStatusInAndNextReviewAtLessThanEqual(
                        userId,
                        EnumSet.of(CardStateStatus.NEW, CardStateStatus.LEARNING, CardStateStatus.REVIEW),
                        Instant.now(),
                        PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "nextReviewAt")))
                .stream()
                .filter(cardStateEntity -> cardStateEntity.getCard().isActive())
                .findFirst()
                .map(this::toDueCardResponse);
    }

    private AgentDueCardResponseDto toDueCardResponse(CardStateEntity cardStateEntity) {
        CardEntity cardEntity = cardStateEntity.getCard();
        return new AgentDueCardResponseDto(
                cardEntity.getId(),
                cardStateEntity.getId(),
                cardEntity.getRussianPrompt(),
                cardEntity.getExampleTranslation(),
                cardEntity.getPartOfSpeech(),
                cardEntity.getLevel(),
                cardStateEntity.getNextReviewAt());
    }
}
