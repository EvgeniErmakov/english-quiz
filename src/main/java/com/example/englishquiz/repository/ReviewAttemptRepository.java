package com.example.englishquiz.repository;

import com.example.englishquiz.domain.ReviewAttemptEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface ReviewAttemptRepository extends JpaRepository<ReviewAttemptEntity, UUID> {

    Page<ReviewAttemptEntity> findByUserIdAndReviewedAtBetween(
            UUID userId,
            Instant from,
            Instant to,
            Pageable pageable);
}
