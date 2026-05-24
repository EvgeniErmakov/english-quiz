package com.example.englishquiz.repository;

import com.example.englishquiz.domain.CardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardRepository extends JpaRepository<CardEntity, UUID> {

    Page<CardEntity> findAllByIsActiveTrue(Pageable pageable);
}
