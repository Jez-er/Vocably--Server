package com.vocably.auth.reset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.vocably.user.User;

/**
 * Writes the reset link to the application log instead of sending email.
 *
 * <p>Stub, deliberately: the reset flow is complete end to end except for delivery, so the link is
 * logged to keep it testable. Logging a working credential is not acceptable in a deployment —
 * replace this bean with a real mail sender before shipping.
 */
@Component
public class LoggingPasswordResetNotifier implements PasswordResetNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingPasswordResetNotifier.class);

    @Override
    public void sendResetLink(User user, String resetUrl) {
        log.warn("Password reset requested for {} — no mailer configured, link: {}", user.getEmail(), resetUrl);
    }
}
