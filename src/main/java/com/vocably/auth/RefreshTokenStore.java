package com.vocably.auth;

import java.util.UUID;

/**
 * Tracks which refresh tokens are still usable.
 *
 * <p>Refresh tokens are rotated on every use, but a JWT stays cryptographically valid until it
 * expires — so without a server-side record, a stolen token keeps working for its full lifetime and
 * logging out does nothing. This is a whitelist: a token is accepted only while its {@code jti} is
 * registered here, so revoking is a delete.
 */
public interface RefreshTokenStore {

    /** Registers a newly issued token as usable. */
    void store(UUID userId, String tokenId);

    boolean isActive(UUID userId, String tokenId);

    /** Revokes one session. */
    void revoke(UUID userId, String tokenId);

    /** Revokes every session for a user — used on logout-everywhere and on password reset. */
    void revokeAll(UUID userId);
}
