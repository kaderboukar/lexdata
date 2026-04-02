package com.lexdata.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Accès aux routes internes (machine-to-machine) via en-tête {@code X-Lexdata-Internal-Key}.
 */
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final String expectedKey;

    public InternalApiKeyFilter(@Value("${lexdata.internal.apiKey:}") String expectedKey) {
        this.expectedKey = expectedKey;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!StringUtils.hasText(expectedKey)) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "lexdata.internal.apiKey non configurée");
            return;
        }
        String provided = request.getHeader("X-Lexdata-Internal-Key");
        if (!expectedKey.equals(provided)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Clé API interne invalide");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
