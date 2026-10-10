package com.vocably.auth.reset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.vocably.user.User;

@Component
public class LoggingPasswordResetNotifier implements PasswordResetNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingPasswordResetNotifier.class);

    @Override
    public void sendResetLink(User user, String resetUrl) {
        log.warn("Password reset requested for {} — no mailer configured, link: {}", user.getEmail(), resetUrl);
    }
}
