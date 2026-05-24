package com.example.englishquiz.controller;

import com.example.englishquiz.dto.AgentDueCardResponseDto;
import com.example.englishquiz.dto.AgentSubmitAnswerResponseDto;
import com.example.englishquiz.dto.ReviewResultDto;
import com.example.englishquiz.dto.SubmitReviewAnswerDto;
import com.example.englishquiz.dto.SubmitReviewAnswerRequestDto;
import com.example.englishquiz.service.AgentReviewService;
import com.example.englishquiz.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agent")
@Validated
public class AgentReviewController {

    private final AgentReviewService agentReviewService;
    private final ReviewService reviewService;

    public AgentReviewController(AgentReviewService agentReviewService, ReviewService reviewService) {
        this.agentReviewService = agentReviewService;
        this.reviewService = reviewService;
    }

    @GetMapping("/due-card")
    public ResponseEntity<AgentDueCardResponseDto> getDueCard(@RequestParam @NotNull UUID userId) {
        return agentReviewService.findNextDueCard(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/answers")
    public ResponseEntity<AgentSubmitAnswerResponseDto> submitAnswer(
            @Valid @RequestBody SubmitReviewAnswerRequestDto requestDto) {
        ReviewResultDto reviewResultDto = reviewService.submitAnswer(new SubmitReviewAnswerDto(
                requestDto.userId(),
                requestDto.cardId(),
                requestDto.userAnswer(),
                requestDto.responseTimeMs()));

        return ResponseEntity.ok(new AgentSubmitAnswerResponseDto(
                reviewResultDto.cardId(),
                reviewResultDto.isCorrect(),
                reviewResultDto.rating(),
                reviewResultDto.normalizedUserAnswer(),
                reviewResultDto.normalizedExpectedAnswer(),
                reviewResultDto.nextReviewAt(),
                reviewResultDto.feedbackMessage()));
    }
}
