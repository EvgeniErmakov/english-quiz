package com.example.englishquiz.controller;

import com.example.englishquiz.dto.ReviewResultDto;
import com.example.englishquiz.dto.SubmitReviewAnswerDto;
import com.example.englishquiz.dto.SubmitReviewAnswerRequestDto;
import com.example.englishquiz.dto.SubmitReviewAnswerResponseDto;
import com.example.englishquiz.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/answers")
    public ResponseEntity<SubmitReviewAnswerResponseDto> submitAnswer(
            @Valid @RequestBody SubmitReviewAnswerRequestDto requestDto) {
        ReviewResultDto reviewResultDto = reviewService.submitAnswer(new SubmitReviewAnswerDto(
                requestDto.userId(),
                requestDto.cardId(),
                requestDto.userAnswer(),
                requestDto.responseTimeMs()));

        SubmitReviewAnswerResponseDto responseDto = new SubmitReviewAnswerResponseDto(
                reviewResultDto.cardId(),
                reviewResultDto.isCorrect(),
                reviewResultDto.rating(),
                reviewResultDto.normalizedUserAnswer(),
                reviewResultDto.normalizedExpectedAnswer(),
                reviewResultDto.nextReviewAt(),
                reviewResultDto.feedbackMessage());

        return ResponseEntity.ok(responseDto);
    }
}
