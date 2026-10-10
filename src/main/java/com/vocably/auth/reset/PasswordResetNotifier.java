package com.vocably.auth.reset;

import com.vocably.user.User;

public interface PasswordResetNotifier {

    void sendResetLink(User user, String resetUrl);
}
