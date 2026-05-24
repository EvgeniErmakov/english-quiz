package com.example.englishquiz.mapper;

import com.example.englishquiz.domain.CardAnswerAliasEntity;
import com.example.englishquiz.domain.CardEntity;
import com.example.englishquiz.dto.CardAliasResponseDto;
import com.example.englishquiz.dto.CardResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CardMapper {

    public CardResponseDto toResponseDto(CardEntity cardEntity, List<CardAnswerAliasEntity> aliasEntities) {
        List<CardAliasResponseDto> aliases = aliasEntities.stream()
                .map(this::toAliasResponseDto)
                .toList();

        return new CardResponseDto(
                cardEntity.getId(),
                cardEntity.getDeck().getId(),
                cardEntity.getRussianPrompt(),
                cardEntity.getEnglishAnswer(),
                cardEntity.getExampleSentence(),
                cardEntity.getExampleTranslation(),
                cardEntity.getPartOfSpeech(),
                cardEntity.getLevel(),
                cardEntity.isActive(),
                aliases,
                cardEntity.getCreatedAt(),
                cardEntity.getUpdatedAt());
    }

    private CardAliasResponseDto toAliasResponseDto(CardAnswerAliasEntity aliasEntity) {
        return new CardAliasResponseDto(aliasEntity.getId(), aliasEntity.getAlias());
    }
}
