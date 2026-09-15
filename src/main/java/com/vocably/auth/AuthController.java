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

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
		AuthResponse userData = authService.registerUser(request);
		setRefreshTokenCookie(response, userData.tokens().refreshToken());

		return userData;
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
		AuthResponse userData = authService.loginUser(request);
		setRefreshTokenCookie(response, userData.tokens().refreshToken());

		return userData;
	}

	@PostMapping("/refresh")
	public TokenResponse refresh(
		@CookieValue(name = "refreshToken") String refreshToken,
		HttpServletResponse response
	) {
		TokenResponse tokens = authService.refresh(refreshToken);
		setRefreshTokenCookie(response, tokens.refreshToken());

		return tokens;
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletResponse response) {
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