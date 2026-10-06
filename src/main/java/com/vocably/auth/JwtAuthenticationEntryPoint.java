package com.vocably.auth;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocably.common.error.ApiErrorResponse;
import com.vocably.common.error.ApiErrors;
import com.vocably.common.error.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Renders a 401 raised inside the security filter chain using the standard error body.
 *
 * <p>Uses the Spring-managed {@link ObjectMapper} rather than a fresh one so the {@code timestamp}
 * serialises as an ISO-8601 string, exactly as it does from {@code GlobalExceptionHandler}.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        ApiErrorResponse body = ApiErrors.of(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED,
                "Authentication is required to access this resource",
                ApiErrors.pathOf(request)
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
