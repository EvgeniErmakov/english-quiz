package com.example.englishquiz.controller;

import com.example.englishquiz.dto.AgentDueCardResponseDto;
import com.example.englishquiz.dto.ReviewResultDto;
import com.example.englishquiz.enums.ReviewRating;
import com.example.englishquiz.exception.GlobalExceptionHandler;
import com.example.englishquiz.exception.NotFoundException;
import com.example.englishquiz.service.AgentReviewService;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AgentReviewControllerTest {

    @Mock
    private AgentReviewService agentReviewService;

    @Mock
    private ReviewService reviewService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AgentReviewController controller = new AgentReviewController(agentReviewService, reviewService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void dueCardWithValidRequestShouldDelegateToService() throws Exception {
        UUID userId = UUID.randomUUID();
        when(agentReviewService.findNextDueCard(userId)).thenReturn(Optional.of(new AgentDueCardResponseDto(
                UUID.randomUUID(), UUID.randomUUID(), "выяснить", null, null, null, Instant.now())));

        mockMvc.perform(get("/api/v1/agent/due-card").param("userId", userId.toString()))
                .andExpect(status().isOk());

        verify(agentReviewService).findNextDueCard(userId);
    }

    @Test
    void dueCardShouldReturnNoContentWhenAbsent() throws Exception {
        UUID userId = UUID.randomUUID();
        when(agentReviewService.findNextDueCard(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/agent/due-card").param("userId", userId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void submitAnswerValidBodyShouldReturnOk() throws Exception {
        when(reviewService.submitAnswer(any())).thenReturn(new ReviewResultDto(
                UUID.randomUUID(), true, ReviewRating.GOOD, "put off", "put off", Instant.now(), "Correct"));

        String json = """
                {
                  \"userId\": \"%s\",
                  \"cardId\": \"%s\",
                  \"userAnswer\": \"put off\",
                  \"responseTimeMs\": 1
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/agent/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(reviewService).submitAnswer(any());
    }

    @Test
    void submitAnswerInvalidBodyShouldReturnBadRequest() throws Exception {
        String json = """
                {
                  \"userId\": \"%s\",
                  \"cardId\": \"%s\",
                  \"userAnswer\": \"\",
                  \"responseTimeMs\": -1
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/agent/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitAnswerNotFoundShouldReturnNotFound() throws Exception {
        when(reviewService.submitAnswer(any())).thenThrow(new NotFoundException("Card not found"));

        String json = """
                {
                  \"userId\": \"%s\",
                  \"cardId\": \"%s\",
                  \"userAnswer\": \"put off\"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/agent/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }
}
