package com.vocably.auth;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.vocably.user.User;

/**
 * The authenticated user as the request sees it.
 *
 * <p>Carries the display name and creation time as well as the id, so {@code GET /api/auth/me} can
 * answer from the security context without a second query — the user row was already loaded to
 * build this.
 */
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String displayName;
    private final String password;
    private final Instant createdAt;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.displayName = user.getDisplayName();
        this.password = user.getPasswordHash();
        this.createdAt = user.getCreatedAt();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
