package com.vocably.auth;

import java.util.UUID;

/**
 * Accepts every token and forgets every revocation.
 *
 * <p>Only wired in when {@code app.auth.refresh-token.revocation-enabled} is explicitly set to
 * {@code false}, which trades away logout and theft containment for not needing Redis — acceptable
 * for a local run, never for a deployment.
 */
public class NoopRefreshTokenStore implements RefreshTokenStore {

    @Override
    public void store(UUID userId, String tokenId) {
        // Nothing is tracked, so nothing to record.
    }

    @Override
    public boolean isActive(UUID userId, String tokenId) {
        return true;
    }

    @Override
    public void revoke(UUID userId, String tokenId) {
        // Nothing is tracked, so nothing to revoke.
    }

    @Override
    public void revokeAll(UUID userId) {
        // Nothing is tracked, so nothing to revoke.
    }
}
