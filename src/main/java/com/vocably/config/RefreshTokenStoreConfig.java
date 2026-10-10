package com.vocably.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.vocably.auth.NoopRefreshTokenStore;
import com.vocably.auth.RedisRefreshTokenStore;
import com.vocably.auth.RefreshTokenStore;

@Configuration
public class RefreshTokenStoreConfig {

    private static final String REVOCATION_ENABLED = "app.auth.refresh-token.revocation-enabled";

    @Bean
    @ConditionalOnProperty(name = REVOCATION_ENABLED, havingValue = "true", matchIfMissing = true)
    public RefreshTokenStore redisRefreshTokenStore(
            StringRedisTemplate redisTemplate,
            @Value("${jwt.refresh-expiration}") long refreshExpirationMillis
    ) {
        return new RedisRefreshTokenStore(redisTemplate, Duration.ofMillis(refreshExpirationMillis));
    }

    @Bean
    @ConditionalOnProperty(name = REVOCATION_ENABLED, havingValue = "false")
    public RefreshTokenStore noopRefreshTokenStore() {
        return new NoopRefreshTokenStore();
    }
}
