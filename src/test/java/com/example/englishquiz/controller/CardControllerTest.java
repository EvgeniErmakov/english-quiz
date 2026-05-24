package com.example.englishquiz.controller;

import com.example.englishquiz.dto.CardResponseDto;
import com.example.englishquiz.dto.CreateCardRequestDto;
import com.example.englishquiz.exception.GlobalExceptionHandler;
import com.example.englishquiz.exception.NotFoundException;
import com.example.englishquiz.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardService cardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CardController controller = new CardController(cardService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldReturnBadRequestOnValidationError() throws Exception {
        String invalidJson = """
                {
                  \"deckId\": \"%s\",
                  \"russianPrompt\": \"\",
                  \"englishAnswer\": \"\"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateCardWithCreatedStatus() throws Exception {
        UUID cardId = UUID.randomUUID();
        CardResponseDto responseDto = new CardResponseDto(
                cardId,
                UUID.randomUUID(),
                "откладывать",
                "put off",
                null,
                null,
                null,
                null,
                true,
                List.of(),
                Instant.now(),
                Instant.now());

        when(cardService.create(any(CreateCardRequestDto.class))).thenReturn(responseDto);

        String json = """
                {
                  \"deckId\": \"%s\",
                  \"russianPrompt\": \"откладывать\",
                  \"englishAnswer\": \"put off\",
                  \"aliases\": [\"postpone\"]
                }
                """.formatted(responseDto.deckId());

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnNotFoundForMissingCard() throws Exception {
        UUID cardId = UUID.randomUUID();
        when(cardService.findById(cardId)).thenThrow(new NotFoundException("Card not found"));

        mockMvc.perform(get("/api/v1/cards/{cardId}", cardId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnPaginatedList() throws Exception {
        Page<CardResponseDto> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(cardService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/cards?page=0&size=10"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeactivateCard() throws Exception {
        UUID cardId = UUID.randomUUID();
        doNothing().when(cardService).deactivate(eq(cardId));

        mockMvc.perform(delete("/api/v1/cards/{cardId}", cardId))
                .andExpect(status().isNoContent());
    }
}
