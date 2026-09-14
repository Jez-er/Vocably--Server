package com.vocably.auth;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vocably.auth.dto.TokenResponse;
import com.vocably.user.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

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

    public String generateAccessToken(User user) {
        return generateToken(user, accessExpiration, "access");
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshExpiration, "refresh");
    }

		public TokenResponse generateTokens(User user) {
				String accessToken = generateAccessToken(user);
				String refreshToken = generateRefreshToken(user);
				return new TokenResponse(accessToken, refreshToken);
		}

		public boolean isRefreshTokenValid(String token) {
    try {
        var claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return "refresh".equals(claims.get("type", String.class));

    } catch (Exception e) {
        return false;
    }
	}

		public String extractUserId(String token) {
    return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
}

    private String generateToken(User user, long expiration, String type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("type", type)
                .signWith(secretKey)
                .compact();
    }
}