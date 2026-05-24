package com.example.englishquiz.service;

import com.example.englishquiz.dto.ReviewResultDto;
import com.example.englishquiz.dto.SubmitReviewAnswerDto;

public interface ReviewService {

    ReviewResultDto submitAnswer(SubmitReviewAnswerDto submitReviewAnswerDto);
}
