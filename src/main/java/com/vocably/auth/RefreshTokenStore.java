package com.vocably.auth;

import java.util.UUID;

public interface RefreshTokenStore {

    void store(UUID userId, String tokenId);

    boolean isActive(UUID userId, String tokenId);

    void revoke(UUID userId, String tokenId);

    void revokeAll(UUID userId);
}
