package com.vocably.auth;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vocably.auth.dto.AuthResponse;
import com.vocably.auth.dto.LoginRequest;
import com.vocably.auth.dto.RegisterRequest;
import com.vocably.auth.dto.TokenResponse;
import com.vocably.user.User;
import com.vocably.user.UserService;
import com.vocably.user.dto.UserResponse;

import jakarta.servlet.http.HttpServletResponse;
@Service 
public class AuthService {
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public AuthResponse registerUser(RegisterRequest request) {
		if (isEmailRegistered(request.email())) {
			throw new IllegalArgumentException("Email is already registered");
		}

		String passwordHash = hashPassword(request.password());
		User user = userService.createUser(request.email(), request.displayName()	, passwordHash);

		TokenResponse tokens = jwtService.generateTokens(user);
		UserResponse userResponse = new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getCreatedAt());

		return new AuthResponse(tokens, userResponse);
	}
	
	public AuthResponse loginUser(LoginRequest request) {
		User user = userService.getUserByEmail(request.email())
				.orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new IllegalArgumentException("Invalid email or password");
		}

		TokenResponse tokens = jwtService.generateTokens(user);
		UserResponse userResponse = new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getCreatedAt());

		return new AuthResponse(tokens, userResponse);
	}

	public void logout(HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie
            .from("refreshToken", "")
            .httpOnly(true)
            .secure(false)
            .sameSite("Strict")
            .path("/api/auth")
            .maxAge(0)
            .build();

    response.addHeader(
            HttpHeaders.SET_COOKIE,
            cookie.toString()
    );
	}

	public TokenResponse refresh(
        String refreshToken,
        HttpServletResponse response
) {
    if (!jwtService.isRefreshTokenValid(refreshToken)) {
        throw new IllegalArgumentException("Invalid refresh token");
    }

	UUID userId = UUID.fromString(jwtService.extractUserId(refreshToken));

    User user = userService.getUserById(userId)
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found")
            );

    String newAccessToken =
            jwtService.generateAccessToken(user);

    String newRefreshToken =
            jwtService.generateRefreshToken(user);

	return new TokenResponse(newAccessToken, newRefreshToken);
}

	private String hashPassword(String password) {
		
		return passwordEncoder.encode(password);
	}
	private boolean isEmailRegistered(String email) {
		return userService.getUserByEmail(email).isPresent();
	}


}
