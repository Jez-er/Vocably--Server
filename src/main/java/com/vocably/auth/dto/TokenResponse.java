package com.vocably.auth.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken
) {}