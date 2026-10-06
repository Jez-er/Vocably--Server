package com.vocably.auth;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vocably.auth.exception.InvalidTokenException;
import com.vocably.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * A refresh token together with the {@code jti} it was minted with.
     *
     * <p>The id is returned rather than re-parsed by the caller because it is what
     * {@link RefreshTokenStore} keys on.
     */
    public record IssuedRefreshToken(String token, String tokenId) {
    }

    /** The identity carried by a verified refresh token. */
    public record RefreshTokenClaims(UUID userId, String tokenId) {
    }

    public String generateAccessToken(User user) {
        return generateToken(user, accessExpiration, ACCESS_TYPE, null);
    }

    /** Mints a refresh token with a unique {@code jti} so a single session can be revoked. */
    public IssuedRefreshToken generateRefreshToken(User user) {
        String tokenId = UUID.randomUUID().toString();

        return new IssuedRefreshToken(generateToken(user, refreshExpiration, REFRESH_TYPE, tokenId), tokenId);
    }

    public boolean isAccessTokenValid(String token) {
        try {
            return ACCESS_TYPE.equals(parse(token).get(TYPE_CLAIM, String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifies a refresh token's signature, expiry, type and shape.
     *
     * @throws InvalidTokenException if the token cannot be trusted; callers must still check the
     *         returned id against {@link RefreshTokenStore} to catch a revoked session
     */
    public RefreshTokenClaims parseRefreshToken(String token) {
        Claims claims;

        try {
            claims = parse(token);
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        if (!REFRESH_TYPE.equals(claims.get(TYPE_CLAIM, String.class))) {
            throw new InvalidTokenException("Token is not a refresh token");
        }

        if (claims.getId() == null || claims.getId().isBlank()) {
            // Tokens minted before per-session revocation existed have no jti and cannot be
            // tracked, so they are no longer accepted.
            throw new InvalidTokenException("Refresh token is missing its identifier");
        }

        try {
            return new RefreshTokenClaims(UUID.fromString(claims.getSubject()), claims.getId());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidTokenException("Refresh token subject is not a user id");
        }
    }

    public String extractUserId(String token) {
        return parse(token).getSubject();
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String generateToken(User user, long expiration, String type, String tokenId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        var builder = Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .claim(TYPE_CLAIM, type);

        if (tokenId != null) {
            builder.id(tokenId);
        }

        return builder
                .signWith(secretKey)
                .compact();
    }
}
