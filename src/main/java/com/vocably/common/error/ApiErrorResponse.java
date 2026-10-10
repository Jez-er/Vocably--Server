package com.vocably.common.error;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ApiError", description = "Error body returned by every endpoint")
public record ApiErrorResponse(

        @Schema(example = "2026-10-06T12:34:56.789Z")
        Instant timestamp,

        @Schema(example = "409")
        int status,

        @Schema(description = "HTTP reason phrase", example = "Conflict")
        String error,

        @Schema(description = "Machine-readable error code", example = "EMAIL_ALREADY_USED")
        String code,

        @Schema(description = "Human-readable description", example = "Email is already registered")
        String message,

        @Schema(example = "/api/auth/register")
        String path,

        @Schema(description = "Validation messages keyed by request field")
        Map<String, String> fieldErrors
) {}
