package com.vocably.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vocably.auth.dto.AuthResponse;
import com.vocably.auth.dto.LoginRequest;
import com.vocably.auth.dto.RegisterRequest;
import com.vocably.auth.dto.TokenResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, login, token refresh and logout")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
		summary = "Register a new user",
		description = "Creates a new user account and returns authentication tokens. A refresh token is set as an HTTP-only cookie."
	)
	@ApiResponse(responseCode = "201", description = "User registered successfully")
	@ApiResponse(responseCode = "401", description = "Registration failed due to invalid credentials or duplicate user")
	public AuthResponse register(
		@Valid @RequestBody RegisterRequest request,
		@Parameter(hidden = true) HttpServletResponse response
	) {
		AuthResponse userData = authService.registerUser(request);
		setRefreshTokenCookie(response, userData.tokens().refreshToken());

		return userData;
	}

	@PostMapping("/login")
	@Operation(
		summary = "Log in an existing user",
		description = "Authenticates a user with their credentials and returns authentication tokens. A refresh token is set as an HTTP-only cookie."
	)
	@ApiResponse(responseCode = "200", description = "User logged in successfully")
	@ApiResponse(responseCode = "401", description = "Invalid username or password")
	public AuthResponse login(
		@Valid @RequestBody LoginRequest request,
		@Parameter(hidden = true) HttpServletResponse response
	) {
		AuthResponse userData = authService.loginUser(request);
		setRefreshTokenCookie(response, userData.tokens().refreshToken());

		return userData;
	}

	@PostMapping("/refresh")
	@Operation(
		summary = "Refresh authentication tokens",
		description = "Uses the refresh token cookie to issue a new pair of access and refresh tokens. The new refresh token is set as an HTTP-only cookie."
	)
	@ApiResponse(responseCode = "200", description = "Tokens refreshed successfully")
	@ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
	public TokenResponse refresh(
		@CookieValue(name = "refreshToken") String refreshToken,
		@Parameter(hidden = true) HttpServletResponse response
	) {
		TokenResponse tokens = authService.refresh(refreshToken);
		setRefreshTokenCookie(response, tokens.refreshToken());

		return tokens;
	}

	@PostMapping("/logout")
	@Operation(
		summary = "Log out the current user",
		description = "Clears the refresh token cookie, effectively logging the user out."
	)
	@ApiResponse(responseCode = "204", description = "User logged out successfully")
	public ResponseEntity<Void> logout(@Parameter(hidden = true) HttpServletResponse response) {
		clearRefreshTokenCookie(response);
		return ResponseEntity.noContent().build();
	}

	private void setRefreshTokenCookie(HttpServletResponse response, String value) {
		ResponseCookie cookie = ResponseCookie.from("refreshToken", value)
				.httpOnly(true)
				.secure(true)
				.path("/")
				.maxAge(7 * 24 * 60 * 60)
				.sameSite("Lax")
				.build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private void clearRefreshTokenCookie(HttpServletResponse response) {
		ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
				.httpOnly(true)
				.secure(true)
				.path("/")
				.maxAge(0)
				.sameSite("Lax")
				.build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}
}