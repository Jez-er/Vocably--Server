package com.vocably.user;

/** How an account authenticates. */
public enum AuthProvider {

    /** Email and password stored in {@code users.password_hash}. */
    LOCAL,

    /**
     * Google sign-in.
     *
     * <p>The column and enum exist so a federated account can be persisted; the sign-in flow itself
     * is still a stub — see {@code OAuth2StubController}.
     */
    GOOGLE
}
