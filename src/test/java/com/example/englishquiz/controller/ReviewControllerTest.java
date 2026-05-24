package com.example.englishquiz.controller;

import com.example.englishquiz.dto.ReviewResultDto;
import com.example.englishquiz.enums.ReviewRating;
import com.example.englishquiz.exception.GlobalExceptionHandler;
import com.example.englishquiz.exception.NotFoundException;
import com.example.englishquiz.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReviewController controller = new ReviewController(reviewService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void validRequestShouldReturnOkAndDelegateToService() throws Exception {
        ReviewResultDto resultDto = new ReviewResultDto(
                UUID.randomUUID(),
                true,
                ReviewRating.GOOD,
                "put off",
                "put off",
                Instant.now().plusSeconds(86400),
                "Correct answer");
        when(reviewService.submitAnswer(any())).thenReturn(resultDto);

        String json = """
                {
                  \"userId\": \"%s\",
                  \"cardId\": \"%s\",
                  \"userAnswer\": \"put off\",
                  \"responseTimeMs\": 500
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/reviews/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(reviewService).submitAnswer(any());
    }

    @Test
    void blankUserAnswerShouldReturnBadRequest() throws Exception {
        String json = """
                {
                  \"userId\": \"%s\",
                  \"cardId\": \"%s\",
                  \"userAnswer\": \"   \"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/reviews/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingUserIdShouldReturnBadRequest() throws Exception {
        String json = """
                {
                  \"cardId\": \"%s\",
                  \"userAnswer\": \"put off\"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/reviews/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingCardIdShouldReturnBadRequest() throws Exception {
        String json = """
                {
                  \"userId\": \"%s\",
                  \"userAnswer\": \"put off\"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/reviews/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void negativeResponseTimeShouldReturnBadRequest() throws Exception {
        String json = """
                {
                  \"userId\": \"%s\",
                  \"cardId\": \"%s\",
                  \"userAnswer\": \"put off\",
                  \"responseTimeMs\": -1
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/reviews/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void serviceNotFoundShouldReturnNotFound() throws Exception {
        when(reviewService.submitAnswer(any())).thenThrow(new NotFoundException("Card not found"));

        String json = """
                {
                  \"userId\": \"%s\",
                  \"cardId\": \"%s\",
                  \"userAnswer\": \"put off\"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/reviews/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }
}
