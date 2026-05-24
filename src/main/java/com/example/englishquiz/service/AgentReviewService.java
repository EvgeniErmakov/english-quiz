package com.example.englishquiz.service;

import com.example.englishquiz.dto.AgentDueCardResponseDto;

import java.util.Optional;
import java.util.UUID;

public interface AgentReviewService {

    Optional<AgentDueCardResponseDto> findNextDueCard(UUID userId);
}
