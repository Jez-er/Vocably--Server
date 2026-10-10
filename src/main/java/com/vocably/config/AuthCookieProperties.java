package com.vocably.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.cookie")
public record AuthCookieProperties(
        String name,
        String path,
        String domain,
        Boolean secure,
        String sameSite
) {
    public AuthCookieProperties {
        name = name == null || name.isBlank() ? "refreshToken" : name;
        path = path == null || path.isBlank() ? "/" : path;
        domain = domain == null || domain.isBlank() ? null : domain;
        secure = secure == null || secure;
        sameSite = sameSite == null || sameSite.isBlank() ? "Lax" : sameSite;
    }
}
