package com.vocably.auth;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.vocably.config.AuthCookieProperties;

@Component
public class RefreshTokenCookieFactory {

    private final AuthCookieProperties properties;
    private final Duration maxAge;

    public RefreshTokenCookieFactory(AuthCookieProperties properties, JwtService jwtService) {
        this.properties = properties;

        this.maxAge = Duration.ofMillis(jwtService.getRefreshExpiration());
    }

    public String cookieName() {
        return properties.name();
    }

    public ResponseCookie create(String refreshToken) {
        return build(refreshToken, maxAge);
    }

    public ResponseCookie clearing() {
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String value, Duration age) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.name(), value)
                .httpOnly(true)
                .secure(properties.secure())
                .path(properties.path())
                .maxAge(age)
                .sameSite(properties.sameSite());

        if (properties.domain() != null) {
            builder.domain(properties.domain());
        }

        return builder.build();
    }
}
