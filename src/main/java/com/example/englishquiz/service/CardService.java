package com.example.englishquiz.service;

import com.example.englishquiz.dto.CardResponseDto;
import com.example.englishquiz.dto.CreateCardRequestDto;
import com.example.englishquiz.dto.UpdateCardRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CardService {

    CardResponseDto create(CreateCardRequestDto requestDto);

    CardResponseDto findById(UUID cardId);

    Page<CardResponseDto> findAll(Pageable pageable);

    CardResponseDto update(UUID cardId, UpdateCardRequestDto requestDto);

    void deactivate(UUID cardId);
}
