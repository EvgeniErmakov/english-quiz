package com.example.englishquiz.service.impl;

import com.example.englishquiz.domain.CardAnswerAliasEntity;
import com.example.englishquiz.domain.CardEntity;
import com.example.englishquiz.domain.DeckEntity;
import com.example.englishquiz.dto.CardResponseDto;
import com.example.englishquiz.dto.CreateCardRequestDto;
import com.example.englishquiz.dto.UpdateCardRequestDto;
import com.example.englishquiz.exception.BadRequestException;
import com.example.englishquiz.exception.NotFoundException;
import com.example.englishquiz.mapper.CardMapper;
import com.example.englishquiz.repository.CardAnswerAliasRepository;
import com.example.englishquiz.repository.CardRepository;
import com.example.englishquiz.repository.DeckRepository;
import com.example.englishquiz.service.AnswerNormalizerService;
import com.example.englishquiz.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final CardAnswerAliasRepository cardAnswerAliasRepository;
    private final CardMapper cardMapper;
    private final AnswerNormalizerService answerNormalizerService;

    public CardServiceImpl(CardRepository cardRepository,
                           DeckRepository deckRepository,
                           CardAnswerAliasRepository cardAnswerAliasRepository,
                           CardMapper cardMapper,
                           AnswerNormalizerService answerNormalizerService) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.cardAnswerAliasRepository = cardAnswerAliasRepository;
        this.cardMapper = cardMapper;
        this.answerNormalizerService = answerNormalizerService;
    }

    @Override
    @Transactional
    public CardResponseDto create(CreateCardRequestDto requestDto) {
        DeckEntity deckEntity = deckRepository.findById(requestDto.deckId())
                .orElseThrow(() -> new NotFoundException("Deck not found"));

        CardEntity cardEntity = new CardEntity();
        cardEntity.setId(UUID.randomUUID());
        cardEntity.setDeck(deckEntity);
        cardEntity.setRussianPrompt(requestDto.russianPrompt());
        cardEntity.setEnglishAnswer(requestDto.englishAnswer());
        cardEntity.setExampleSentence(requestDto.exampleSentence());
        cardEntity.setExampleTranslation(requestDto.exampleTranslation());
        cardEntity.setPartOfSpeech(requestDto.partOfSpeech());
        cardEntity.setLevel(requestDto.level());
        cardEntity.setActive(true);
        cardEntity.setCreatedAt(Instant.now());
        cardEntity.setUpdatedAt(Instant.now());

        CardEntity savedCard = cardRepository.save(cardEntity);
        List<CardAnswerAliasEntity> aliases = saveAliases(savedCard, requestDto.aliases());
        return cardMapper.toResponseDto(savedCard, aliases);
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponseDto findById(UUID cardId) {
        CardEntity cardEntity = findExistingCard(cardId);
        List<CardAnswerAliasEntity> aliases = cardAnswerAliasRepository.findAllByCard(cardEntity);
        return cardMapper.toResponseDto(cardEntity, aliases);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponseDto> findAll(Pageable pageable) {
        return cardRepository.findAllByIsActiveTrue(pageable)
                .map(cardEntity -> cardMapper.toResponseDto(cardEntity, cardAnswerAliasRepository.findAllByCard(cardEntity)));
    }

    @Override
    @Transactional
    public CardResponseDto update(UUID cardId, UpdateCardRequestDto requestDto) {
        CardEntity cardEntity = findExistingCard(cardId);

        cardEntity.setRussianPrompt(requestDto.russianPrompt());
        cardEntity.setEnglishAnswer(requestDto.englishAnswer());
        cardEntity.setExampleSentence(requestDto.exampleSentence());
        cardEntity.setExampleTranslation(requestDto.exampleTranslation());
        cardEntity.setPartOfSpeech(requestDto.partOfSpeech());
        cardEntity.setLevel(requestDto.level());
        cardEntity.setUpdatedAt(Instant.now());

        CardEntity updatedCard = cardRepository.save(cardEntity);
        cardAnswerAliasRepository.deleteAllByCard(updatedCard);
        List<CardAnswerAliasEntity> aliases = saveAliases(updatedCard, requestDto.aliases());

        return cardMapper.toResponseDto(updatedCard, aliases);
    }

    @Override
    @Transactional
    public void deactivate(UUID cardId) {
        CardEntity cardEntity = findExistingCard(cardId);
        cardEntity.setActive(false);
        cardEntity.setUpdatedAt(Instant.now());
        cardRepository.save(cardEntity);
    }

    private CardEntity findExistingCard(UUID cardId) {
        return cardRepository.findById(cardId)
                .filter(CardEntity::isActive)
                .orElseThrow(() -> new NotFoundException("Card not found"));
    }

    private List<CardAnswerAliasEntity> saveAliases(CardEntity cardEntity, List<String> aliasValues) {
        if (aliasValues == null || aliasValues.isEmpty()) {
            return List.of();
        }

        Set<String> uniqueNormalizedAliases = new HashSet<>();
        for (String aliasValue : aliasValues) {
            String normalizedAlias = answerNormalizerService.normalize(aliasValue);
            if (normalizedAlias.isBlank()) {
                throw new BadRequestException("Alias cannot be blank");
            }
            if (!uniqueNormalizedAliases.add(normalizedAlias)) {
                throw new BadRequestException("Duplicate aliases are not allowed");
            }
        }

        return aliasValues.stream()
                .map(aliasValue -> createAliasEntity(cardEntity, aliasValue))
                .map(cardAnswerAliasRepository::save)
                .toList();
    }

    private CardAnswerAliasEntity createAliasEntity(CardEntity cardEntity, String aliasValue) {
        CardAnswerAliasEntity aliasEntity = new CardAnswerAliasEntity();
        aliasEntity.setId(UUID.randomUUID());
        aliasEntity.setCard(cardEntity);
        aliasEntity.setAlias(aliasValue.trim());
        aliasEntity.setCreatedAt(Instant.now());
        return aliasEntity;
    }
}
