package com.vocably.config;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vocably.auth.CustomUserDetailsService;
import com.vocably.auth.JwtAuthenticationEntryPoint;
import com.vocably.auth.JwtAuthenticationFilter;
import com.vocably.auth.JwtService;
import com.vocably.auth.UserPrincipal;
import com.vocably.common.HealthController;
import com.vocably.user.User;
import com.vocably.word.WordController;
import com.vocably.word.WordService;

@WebMvcTest(controllers = {HealthController.class, WordController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private WordService wordService;

    @Test
    void publicEndpoint_health_isPermittedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withoutToken_returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/words"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withValidToken_returns200Ok() throws Exception {
        String token = "valid-jwt-access-token";
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");
        user.setPasswordHash("hashedpass");
        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(jwtService.isAccessTokenValid(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(userId.toString());
        when(customUserDetailsService.loadUserById(userId)).thenReturn(userPrincipal);

        mockMvc.perform(get("/api/words")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
