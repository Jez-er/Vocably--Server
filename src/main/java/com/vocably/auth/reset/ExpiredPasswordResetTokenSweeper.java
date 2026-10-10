package com.vocably.auth.reset;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExpiredPasswordResetTokenSweeper {

    private static final Logger log = LoggerFactory.getLogger(ExpiredPasswordResetTokenSweeper.class);

    private final PasswordResetTokenRepository tokenRepository;

    public ExpiredPasswordResetTokenSweeper(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(cron = "${app.auth.password-reset.sweep-cron:0 15 3 * * *}")
    @Transactional
    public void sweep() {
        int removed = tokenRepository.deleteExpiredOrUsed(Instant.now());

        if (removed > 0) {
            log.info("Removed {} spent or expired password reset token(s)", removed);
        }
    }
}
