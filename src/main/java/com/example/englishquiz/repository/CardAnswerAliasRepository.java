package com.example.englishquiz.repository;

import com.example.englishquiz.domain.CardAnswerAliasEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardAnswerAliasRepository extends JpaRepository<CardAnswerAliasEntity, UUID> {
}
