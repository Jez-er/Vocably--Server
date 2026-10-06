package com.vocably.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    @NotBlank
    @Email
    String email,

    // No @Size here, unlike RegisterRequest. Length is a rule about choosing a password, not about
    // presenting one: enforcing it on login would answer a too-short password with 400 instead of
    // 401, and would permanently lock out any account whose password predates the current limits.
    @NotBlank
    String password

) {}
