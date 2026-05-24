package com.example.englishquiz.service;

import com.example.englishquiz.dto.AnswerCheckRequestDto;
import com.example.englishquiz.dto.AnswerCheckResultDto;

public interface AnswerCheckingService {

    AnswerCheckResultDto checkAnswer(AnswerCheckRequestDto requestDto);
}
