package com.vocably.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vocably.common.error.ApiErrorResponse;
import com.vocably.common.error.NotImplementedException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Placeholder for federated sign-in.
 *
 * <p>Deliberately a stub. The endpoints exist and their paths are fixed, so the client's "Continue
 * with Google" button has a defined destination and a defined failure, instead of a handler that
 * does nothing. Both answer 501 with {@code code: NOT_IMPLEMENTED}.
 *
 * <p>The persistence side is already in place — {@code users.password_hash} is nullable and
 * {@code provider}/{@code provider_id} exist (migration V4), so a federated account can be stored.
 * What is missing is the flow itself: the {@code spring-boot-starter-oauth2-client} dependency, the
 * {@code spring.security.oauth2.client.*} registration, and an
 * {@code AuthenticationSuccessHandler} that mints our own JWTs and redirects to the frontend.
 */
@RestController
@RequestMapping("/api/auth/oauth2")
@Tag(name = "Authentication")
public class OAuth2StubController {

    @GetMapping("/{provider}")
    @Operation(
            summary = "Start federated sign-in (not implemented)",
            description = "Reserved for the redirect to the identity provider. Returns 501 until OAuth2 is wired up."
    )
    @ApiResponse(responseCode = "501", description = "Federated sign-in is not configured (code NOT_IMPLEMENTED)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public void authorize(
            @Parameter(description = "Identity provider, e.g. google") @PathVariable String provider
    ) {
        throw new NotImplementedException(
                "Sign-in with " + provider + " is not configured on this server yet"
        );
    }

    @GetMapping("/callback/{provider}")
    @Operation(
            summary = "Federated sign-in callback (not implemented)",
            description = "Reserved for the provider's redirect back. Returns 501 until OAuth2 is wired up."
    )
    @ApiResponse(responseCode = "501", description = "Federated sign-in is not configured (code NOT_IMPLEMENTED)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public void callback(
            @Parameter(description = "Identity provider, e.g. google") @PathVariable String provider
    ) {
        throw new NotImplementedException(
                "Sign-in with " + provider + " is not configured on this server yet"
        );
    }
}
