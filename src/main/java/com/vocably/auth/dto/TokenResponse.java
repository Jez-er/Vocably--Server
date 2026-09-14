package com.vocably.auth.dto;

public record TokenResponse(
    String refreshToken,
    String accessToken
) {}