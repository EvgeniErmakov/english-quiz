package com.example.englishquiz.repository;

import com.example.englishquiz.domain.CardStateEntity;
import com.example.englishquiz.enums.CardStateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface CardStateRepository extends JpaRepository<CardStateEntity, UUID> {

    Page<CardStateEntity> findByUserIdAndStatusInAndNextReviewAtLessThanEqual(
            UUID userId,
            Collection<CardStateStatus> statuses,
            Instant now,
            Pageable pageable);

    Optional<CardStateEntity> findByUserIdAndCardId(UUID userId, UUID cardId);
}
