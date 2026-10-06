package com.vocably.auth.reset;

import com.vocably.user.User;

/**
 * Delivers a reset link to the user.
 *
 * <p>An interface with one logging implementation for now: no mail transport is configured yet, and
 * this is the single seam to replace when one is.
 */
public interface PasswordResetNotifier {

    void sendResetLink(User user, String resetUrl);
}
