package com.vocably.auth.reset;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.vocably.auth.RefreshTokenStore;
import com.vocably.auth.exception.InvalidTokenException;
import com.vocably.user.User;
import com.vocably.user.UserService;

@Service
public class PasswordResetService {

    private static final int TOKEN_BYTES = 32;

    private final UserService userService;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetNotifier notifier;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenStore refreshTokenStore;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Duration tokenTtl;
    private final String resetUrlTemplate;

    public PasswordResetService(
            UserService userService,
            PasswordResetTokenRepository tokenRepository,
            PasswordResetNotifier notifier,
            PasswordEncoder passwordEncoder,
            RefreshTokenStore refreshTokenStore,
            @Value("${app.auth.password-reset.ttl:PT30M}") Duration tokenTtl,
            @Value("${app.frontend.reset-password-url:http://localhost:3000/reset-password}") String resetUrlTemplate
    ) {
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.notifier = notifier;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenStore = refreshTokenStore;
        this.tokenTtl = tokenTtl;
        this.resetUrlTemplate = resetUrlTemplate;
    }

    @Transactional
    public void requestReset(String email) {
        Optional<User> maybeUser = userService.getUserByEmail(email);

        if (maybeUser.isEmpty()) {
            return;
        }

        User user = maybeUser.get();
        String rawToken = generateToken();

        tokenRepository.deleteUnusedByUserId(user.getId());

        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getId());
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(Instant.now().plus(tokenTtl));
        tokenRepository.save(token);

        notifier.sendResetLink(user, buildResetUrl(rawToken));
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        if (!token.isUsable(Instant.now())) {
            throw new InvalidTokenException("Invalid or expired password reset token");
        }

        User user = userService.getUserById(token.getUserId())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        userService.updatePasswordHash(user, passwordEncoder.encode(newPassword));

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);

        refreshTokenStore.revokeAll(user.getId());
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String buildResetUrl(String rawToken) {
        return UriComponentsBuilder.fromUriString(resetUrlTemplate)
                .queryParam("token", rawToken)
                .build()
                .toUriString();
    }
}
