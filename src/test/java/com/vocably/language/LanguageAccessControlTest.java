package com.vocably.language;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vocably.auth.ApiAccessDeniedHandler;
import com.vocably.auth.CustomUserDetailsService;
import com.vocably.auth.JwtAuthenticationEntryPoint;
import com.vocably.auth.JwtAuthenticationFilter;
import com.vocably.auth.JwtService;
import com.vocably.config.CorsProperties;
import com.vocably.config.SecurityConfig;
import com.vocably.language.dto.LanguageResponse;

@WebMvcTest(controllers = LanguageController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        ApiAccessDeniedHandler.class
})
@EnableConfigurationProperties(CorsProperties.class)
class LanguageAccessControlTest {

    private static final String BODY = """
            {"title":"Klingon","code":"tlh","flag":"🏴"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LanguageService languageService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    void create_asOrdinaryUser_isForbidden() throws Exception {
        mockMvc.perform(post("/api/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        verify(languageService, never()).createLanguage(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_isAllowed() throws Exception {
        when(languageService.createLanguage(any()))
                .thenReturn(new LanguageResponse(UUID.randomUUID(), "Klingon", "tlh", "🏴"));

        mockMvc.perform(post("/api/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("tlh"));
    }

    @Test
    void create_withoutAuthentication_is401NotYetForbidden() throws Exception {
        mockMvc.perform(post("/api/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void reading_staysOpenToAnySignedInUser() throws Exception {
        when(languageService.getAllLanguages()).thenReturn(List.of());

        mockMvc.perform(get("/api/languages"))
                .andExpect(status().isOk());
    }
}
