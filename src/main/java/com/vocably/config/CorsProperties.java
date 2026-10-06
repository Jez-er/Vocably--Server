package com.vocably.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Browser origins allowed to call this API.
 *
 * <p>Origins are listed explicitly rather than wildcarded because the API answers credentialed
 * requests (the refresh-token cookie), and a wildcard origin is invalid with
 * {@code Access-Control-Allow-Credentials: true}.
 *
 * @param allowedOrigins exact origins, e.g. {@code https://app.example.com}
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }
}
