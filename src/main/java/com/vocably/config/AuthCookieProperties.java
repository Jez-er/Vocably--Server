package com.vocably.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * How the refresh-token cookie is written.
 *
 * <p>{@code sameSite} has to be configurable: on a single origin (or behind the Next.js rewrite)
 * {@code Lax} is correct and the stricter option, but once the app is served from
 * {@code app.example.com} and the API from {@code api.example.com}, a {@code Lax} cookie is simply
 * never sent on the cross-site XHR, so the session silently cannot be refreshed. That deploy needs
 * {@code None}, which browsers only honour together with {@code Secure}.
 *
 * @param name     cookie name
 * @param path     cookie path
 * @param domain   cookie domain, or null to scope it to the exact API host
 * @param secure   whether to set the {@code Secure} attribute
 * @param sameSite {@code Lax}, {@code Strict} or {@code None}
 */
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
