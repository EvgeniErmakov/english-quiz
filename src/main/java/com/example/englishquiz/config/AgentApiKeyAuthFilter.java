package com.example.englishquiz.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AgentApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String AGENT_HEADER = "X-Agent-Api-Key";

    private final String configuredApiKey;

    public AgentApiKeyAuthFilter(@Value("${agent.api-key:}") String configuredApiKey) {
        this.configuredApiKey = configuredApiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/agent/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String providedApiKey = request.getHeader(AGENT_HEADER);

        if (configuredApiKey == null || configuredApiKey.isBlank()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Agent API key is not configured");
            return;
        }

        if (providedApiKey == null || !configuredApiKey.equals(providedApiKey)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid agent API key");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
