package com.vocably.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vocably.auth.JwtService.IssuedRefreshToken;
import com.vocably.auth.JwtService.RefreshTokenClaims;
import com.vocably.auth.dto.AuthResponse;
import com.vocably.auth.dto.LoginRequest;
import com.vocably.auth.dto.RegisterRequest;
import com.vocably.auth.dto.TokenResponse;
import com.vocably.auth.exception.EmailAlreadyUsedException;
import com.vocably.auth.exception.InvalidCredentialsException;
import com.vocably.auth.exception.InvalidTokenException;
import com.vocably.user.User;
import com.vocably.user.UserService;
import com.vocably.user.dto.UserResponse;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    public AuthService(
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenStore refreshTokenStore
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (isEmailRegistered(normalizedEmail)) {
            throw new EmailAlreadyUsedException();
        }

        String passwordHash = hashPassword(request.password());
        User user = userService.createUser(normalizedEmail, request.displayName().trim(), passwordHash);

        return new AuthResponse(issueTokens(user), UserResponse.from(user));
    }

    public AuthResponse loginUser(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userService.getUserByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.hasPassword() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResponse(issueTokens(user), UserResponse.from(user));
    }

    public TokenResponse refresh(String refreshToken) {
        RefreshTokenClaims claims = jwtService.parseRefreshToken(refreshToken);

        if (!refreshTokenStore.isActive(claims.userId(), claims.tokenId())) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        User user = userService.getUserById(claims.userId())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired refresh token"));

        refreshTokenStore.revoke(claims.userId(), claims.tokenId());

        return issueTokens(user);
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        try {
            RefreshTokenClaims claims = jwtService.parseRefreshToken(refreshToken);
            refreshTokenStore.revoke(claims.userId(), claims.tokenId());
        } catch (InvalidTokenException e) {
        }
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        IssuedRefreshToken refresh = jwtService.generateRefreshToken(user);

        refreshTokenStore.store(user.getId(), refresh.tokenId());

        return new TokenResponse(accessToken, refresh.token());
    }

    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    private boolean isEmailRegistered(String email) {
        return userService.getUserByEmail(email).isPresent();
    }
}
