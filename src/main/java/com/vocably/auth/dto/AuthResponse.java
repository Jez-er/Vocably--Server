package com.vocably.auth.dto;

import com.vocably.user.dto.UserResponse;


public record AuthResponse(
    TokenResponse tokens,
    UserResponse user
) {}