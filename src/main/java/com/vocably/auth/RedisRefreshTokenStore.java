package com.vocably.auth;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis-backed whitelist, one set of live {@code jti}s per user.
 *
 * <p>A set rather than a key per token: revoking every session for a user is then a single DEL
 * instead of a KEYS scan. The set's TTL is pushed forward on each issue, so it outlives the newest
 * token; ids of tokens that have since expired may linger in it, which is harmless because
 * {@link JwtService#parseRefreshToken} rejects an expired token before this store is consulted.
 */
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redis;
    private final Duration ttl;

    public RedisRefreshTokenStore(StringRedisTemplate redis, Duration ttl) {
        this.redis = redis;
        this.ttl = ttl;
    }

    @Override
    public void store(UUID userId, String tokenId) {
        String key = keyFor(userId);

        redis.opsForSet().add(key, tokenId);
        redis.expire(key, ttl);
    }

    @Override
    public boolean isActive(UUID userId, String tokenId) {
        return Boolean.TRUE.equals(redis.opsForSet().isMember(keyFor(userId), tokenId));
    }

    @Override
    public void revoke(UUID userId, String tokenId) {
        redis.opsForSet().remove(keyFor(userId), tokenId);
    }

    @Override
    public void revokeAll(UUID userId) {
        redis.delete(keyFor(userId));
    }

    private String keyFor(UUID userId) {
        return KEY_PREFIX + userId;
    }
}
