package com.example.englishquiz;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.liquibase.enabled=false",
        "agent.api-key=test-agent-key"
})
class AgentApiKeySecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .apply(springSecurity())
                .build();
    }

    @Test
    void dueCardWithoutApiKeyShouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/v1/agent/due-card").param("userId", "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dueCardWithInvalidApiKeyShouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/v1/agent/due-card")
                        .param("userId", "00000000-0000-0000-0000-000000000001")
                        .header("X-Agent-Api-Key", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dueCardWithValidApiKeyShouldReachController() throws Exception {
        mockMvc.perform(get("/api/v1/agent/due-card")
                        .param("userId", "00000000-0000-0000-0000-000000000001")
                        .header("X-Agent-Api-Key", "test-agent-key"))
                .andExpect(status().isNoContent());
    }

    @Test
    void postAnswerWithoutApiKeyShouldBeRejected() throws Exception {
        mockMvc.perform(post("/api/v1/agent/answers")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
