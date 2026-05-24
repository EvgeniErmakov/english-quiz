package com.example.englishquiz.controller;

import com.example.englishquiz.dto.CardResponseDto;
import com.example.englishquiz.dto.CreateCardRequestDto;
import com.example.englishquiz.dto.UpdateCardRequestDto;
import com.example.englishquiz.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<CardResponseDto> create(@Valid @RequestBody CreateCardRequestDto requestDto) {
        CardResponseDto responseDto = cardService.create(requestDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{cardId}")
                .buildAndExpand(responseDto.id())
                .toUri();
        return ResponseEntity.created(location).body(responseDto);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponseDto> findById(@PathVariable UUID cardId) {
        return ResponseEntity.ok(cardService.findById(cardId));
    }

    @GetMapping
    public ResponseEntity<Page<CardResponseDto>> findAll(Pageable pageable) {
        return ResponseEntity.ok(cardService.findAll(pageable));
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CardResponseDto> update(@PathVariable UUID cardId,
                                                   @Valid @RequestBody UpdateCardRequestDto requestDto) {
        return ResponseEntity.ok(cardService.update(cardId, requestDto));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID cardId) {
        cardService.deactivate(cardId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
