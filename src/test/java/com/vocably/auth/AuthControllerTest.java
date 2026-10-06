package com.vocably.auth;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vocably.auth.exception.EmailAlreadyUsedException;
import com.vocably.auth.exception.InvalidCredentialsException;
import com.vocably.auth.reset.PasswordResetService;
import com.vocably.config.AuthCookieProperties;
import com.vocably.config.CorsProperties;
import com.vocably.config.SecurityConfig;
import com.vocably.user.User;

/**
 * Covers the status codes and error body the client branches on.
 *
 * <p>These used to be 500s: the service threw {@code IllegalArgumentException} for every rejection
 * and nothing translated it.
 */
@WebMvcTest(controllers = {AuthController.class, OAuth2StubController.class})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        ApiAccessDeniedHandler.class,
        RefreshTokenCookieFactory.class
})
@EnableConfigurationProperties({CorsProperties.class, AuthCookieProperties.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void register_withAlreadyUsedEmail_returns409WithCode() throws Exception {
        when(authService.registerUser(any())).thenThrow(new EmailAlreadyUsedException());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"taken@example.com","displayName":"Taken","password":"password123"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_USED"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void login_withWrongPassword_returns401WithCode() throws Exception {
        when(authService.loginUser(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"wrongpassword"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void register_withInvalidBody_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","displayName":"a","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.email").isString())
                .andExpect(jsonPath("$.fieldErrors.displayName").isString())
                .andExpect(jsonPath("$.fieldErrors.password").isString());
    }

    @Test
    void login_withOverlongPassword_isAnAuthenticationFailureNotAValidationFailure() throws Exception {
        // LoginRequest deliberately has no @Size: an account whose password predates the current
        // limits must still be able to log in, and a wrong password is a 401 either way.
        when(authService.loginUser(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"a-password-well-over-twenty-characters"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void login_withBlankPassword_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.password").isString());
    }

    @Test
    void refresh_withoutCookie_returns401WithCode() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_MISSING"));
    }

    @Test
    void me_withValidToken_returnsTheAuthenticatedUser() throws Exception {
        String token = "valid-access-token";
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-02T03:04:05Z");

        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");
        user.setDisplayName("User");
        user.setPasswordHash("hashedpass");
        user.setCreatedAt(createdAt);

        when(jwtService.isAccessTokenValid(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(userId.toString());
        when(customUserDetailsService.loadUserById(userId)).thenReturn(new UserPrincipal(user));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.displayName").value("User"))
                .andExpect(jsonPath("$.createdAt").value("2026-01-02T03:04:05Z"));
    }

    @Test
    void oauthStub_returns501WithCode() throws Exception {
        mockMvc.perform(get("/api/auth/oauth2/google"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.code").value("NOT_IMPLEMENTED"));
    }

    @Test
    void forgotPassword_returns202RegardlessOfWhetherTheEmailExists() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nobody@example.com"}
                                """))
                .andExpect(status().isAccepted());
    }

    @Test
    void logout_withoutCookie_stillSucceeds() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent());
    }
}
