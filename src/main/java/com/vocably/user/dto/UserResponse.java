package com.vocably.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.vocably.auth.UserPrincipal;
import com.vocably.user.User;

public record UserResponse(
    UUID id,
    String email,
    String displayName,
    Instant createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getCreatedAt());
    }

    public static UserResponse from(UserPrincipal principal) {
        return new UserResponse(
                principal.getId(),
                principal.getEmail(),
                principal.getDisplayName(),
                principal.getCreatedAt()
        );
    }
}
