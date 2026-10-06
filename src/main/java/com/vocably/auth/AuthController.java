package com.vocably.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vocably.auth.dto.AuthResponse;
import com.vocably.auth.dto.ForgotPasswordRequest;
import com.vocably.auth.dto.LoginRequest;
import com.vocably.auth.dto.RegisterRequest;
import com.vocably.auth.dto.ResetPasswordRequest;
import com.vocably.auth.dto.TokenResponse;
import com.vocably.auth.reset.PasswordResetService;
import com.vocably.common.error.ApiErrorResponse;
import com.vocably.user.dto.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, login, session and password reset")
public class AuthController {

	private final AuthService authService;
	private final PasswordResetService passwordResetService;
	private final RefreshTokenCookieFactory refreshTokenCookieFactory;

	public AuthController(
			AuthService authService,
			PasswordResetService passwordResetService,
			RefreshTokenCookieFactory refreshTokenCookieFactory
	) {
		this.authService = authService;
		this.passwordResetService = passwordResetService;
		this.refreshTokenCookieFactory = refreshTokenCookieFactory;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
		summary = "Register a new user",
		description = "Creates a new user account and returns authentication tokens. A refresh token is set as an HTTP-only cookie."
	)
	@ApiResponse(responseCode = "201", description = "User registered successfully")
	@ApiResponse(responseCode = "400", description = "Validation failed (code VALIDATION_FAILED, with fieldErrors)",
		content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
	@ApiResponse(responseCode = "409", description = "Email is already registered (code EMAIL_ALREADY_USED)",
		content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
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
	@ApiResponse(responseCode = "400", description = "Validation failed (code VALIDATION_FAILED, with fieldErrors)",
		content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
	@ApiResponse(responseCode = "401", description = "Invalid email or password (code INVALID_CREDENTIALS)",
		content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
	public AuthResponse login(
		@Valid @RequestBody LoginRequest request,
		@Parameter(hidden = true) HttpServletResponse response
	) {
		AuthResponse userData = authService.loginUser(request);
		setRefreshTokenCookie(response, userData.tokens().refreshToken());

		return userData;
	}

	@GetMapping("/me")
	@SecurityRequirement(name = "BearerAuth")
	@Operation(
		summary = "Get the authenticated user",
		description = "Returns the user the access token belongs to. Lets a client re-establish who is logged in after a page reload, instead of trusting a copy held in local storage."
	)
	@ApiResponse(responseCode = "200", description = "Current user returned")
	@ApiResponse(responseCode = "401", description = "Missing or invalid access token (code UNAUTHORIZED)",
		content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
	public UserResponse me(@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
		return UserResponse.from(principal);
	}

	@PostMapping("/refresh")
	@Operation(
		summary = "Refresh authentication tokens",
		description = "Uses the refresh token cookie to issue a new pair of access and refresh tokens. The presented token is revoked, and the new refresh token is set as an HTTP-only cookie."
	)
	@ApiResponse(responseCode = "200", description = "Tokens refreshed successfully")
	@ApiResponse(responseCode = "401", description = "Missing, invalid, expired or revoked refresh token (codes REFRESH_TOKEN_MISSING, INVALID_TOKEN)",
		content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
	public TokenResponse refresh(
		@CookieValue(name = "${app.auth.cookie.name:refreshToken}") String refreshToken,
		@Parameter(hidden = true) HttpServletResponse response
	) {
		TokenResponse tokens = authService.refresh(refreshToken);
		setRefreshTokenCookie(response, tokens.refreshToken());

		return tokens;
	}

	@PostMapping("/logout")
	@Operation(
		summary = "Log out the current user",
		description = "Revokes the presented refresh token server-side and clears its cookie. Succeeds even when no valid token is presented."
	)
	@ApiResponse(responseCode = "204", description = "User logged out successfully")
	public ResponseEntity<Void> logout(
		@CookieValue(name = "${app.auth.cookie.name:refreshToken}", required = false) String refreshToken,
		@Parameter(hidden = true) HttpServletResponse response
	) {
		authService.logout(refreshToken);
		clearRefreshTokenCookie(response);

		return ResponseEntity.noContent().build();
	}

	@PostMapping("/forgot-password")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Operation(
		summary = "Request a password reset link",
		description = "Sends a reset link to the address if it has an account. Answers 202 either way: reporting whether an address is registered would make this an account-enumeration oracle. Delivery is currently stubbed — the link is written to the server log."
	)
	@ApiResponse(responseCode = "202", description = "Request accepted")
	@ApiResponse(responseCode = "400", description = "Validation failed (code VALIDATION_FAILED, with fieldErrors)",
		content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
	public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		passwordResetService.requestReset(request.email());
	}

	@PostMapping("/reset-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(
		summary = "Set a new password using a reset token",
		description = "Consumes a reset token and replaces the account password. All existing sessions for that account are revoked."
	)
	@ApiResponse(responseCode = "204", description = "Password changed")
	@ApiResponse(responseCode = "400", description = "Validation failed (code VALIDATION_FAILED, with fieldErrors)",
		content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
	@ApiResponse(responseCode = "401", description = "Token is unknown, expired or already used (code INVALID_TOKEN)",
		content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
	public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		passwordResetService.resetPassword(request.token(), request.password());
	}

	private void setRefreshTokenCookie(HttpServletResponse response, String value) {
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.create(value).toString());
	}

	private void clearRefreshTokenCookie(HttpServletResponse response) {
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.clearing().toString());
	}
}
