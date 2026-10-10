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
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";

    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(decodeSecret(secret));

        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public record IssuedRefreshToken(String token, String tokenId) {
    }

    public record RefreshTokenClaims(UUID userId, String tokenId) {
    }

    public record AccessTokenClaims(UUID userId) {
    }

    public String generateAccessToken(User user) {
        return generateToken(user, accessExpiration, ACCESS_TYPE, null);
    }

    public IssuedRefreshToken generateRefreshToken(User user) {
        String tokenId = UUID.randomUUID().toString();

        return new IssuedRefreshToken(generateToken(user, refreshExpiration, REFRESH_TYPE, tokenId), tokenId);
    }

    public AccessTokenClaims parseAccessToken(String token) {
        Claims claims;

        try {
            claims = parse(token);
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid or expired access token");
        }

        if (!ACCESS_TYPE.equals(claims.get(TYPE_CLAIM, String.class))) {

            throw new InvalidTokenException("Token is not an access token");
        }

        try {
            return new AccessTokenClaims(UUID.fromString(claims.getSubject()));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidTokenException("Access token subject is not a user id");
        }
    }

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
            throw new InvalidTokenException("Refresh token is missing its identifier");
        }

        try {
            return new RefreshTokenClaims(UUID.fromString(claims.getSubject()), claims.getId());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidTokenException("Refresh token subject is not a user id");
        }
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    private static byte[] decodeSecret(String secret) {
        byte[] key;

        try {
            key = Decoders.BASE64.decode(secret);
        } catch (DecodingException e) {
            throw new IllegalStateException(
                    "jwt.secret is not valid base64. Generate one with: openssl rand -base64 48", e);
        }

        if (key.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret decodes to " + key.length + " bytes, which is too short for HS256; "
                            + "at least " + MIN_SECRET_BYTES + " are required. "
                            + "Generate one with: openssl rand -base64 48");
        }

        return key;
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
