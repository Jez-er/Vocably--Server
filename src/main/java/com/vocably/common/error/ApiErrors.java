package com.vocably.common.error;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;

/** Builds {@link ApiErrorResponse} instances so every producer emits an identical shape. */
public final class ApiErrors {

    private ApiErrors() {
    }

    public static ApiErrorResponse of(HttpStatus status, ErrorCode code, String message, String path) {
        return of(status, code, message, path, null);
    }

    public static ApiErrorResponse of(
            HttpStatus status,
            ErrorCode code,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code.name(),
                message,
                path,
                fieldErrors == null || fieldErrors.isEmpty() ? null : fieldErrors
        );
    }

    /** The path to report for a request. */
    public static String pathOf(HttpServletRequest request) {
        return request == null ? null : request.getRequestURI();
    }
}
