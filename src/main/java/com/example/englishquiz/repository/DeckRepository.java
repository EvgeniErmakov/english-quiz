package com.example.englishquiz.repository;

import com.example.englishquiz.domain.DeckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeckRepository extends JpaRepository<DeckEntity, UUID> {
}
