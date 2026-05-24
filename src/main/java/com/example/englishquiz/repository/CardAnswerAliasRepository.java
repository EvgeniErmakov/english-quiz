package com.example.englishquiz.repository;

import com.example.englishquiz.domain.CardAnswerAliasEntity;
import com.example.englishquiz.domain.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardAnswerAliasRepository extends JpaRepository<CardAnswerAliasEntity, UUID> {

    List<CardAnswerAliasEntity> findAllByCard(CardEntity cardEntity);

    void deleteAllByCard(CardEntity cardEntity);
}
