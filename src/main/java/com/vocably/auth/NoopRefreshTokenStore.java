package com.vocably.auth;

import java.util.UUID;

public class NoopRefreshTokenStore implements RefreshTokenStore {

    @Override
    public void store(UUID userId, String tokenId) {
    }

    @Override
    public boolean isActive(UUID userId, String tokenId) {
        return true;
    }

    @Override
    public void revoke(UUID userId, String tokenId) {
    }

    @Override
    public void revokeAll(UUID userId) {
    }
}
