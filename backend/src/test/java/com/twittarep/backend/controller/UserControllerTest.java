package com.twittarep.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.twittarep.backend.dto.MeResponse;
import com.twittarep.backend.service.CurrentUserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(com.twittarep.backend.config.SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private com.twittarep.backend.security.CustomJwtAuthenticationConverter jwtAuthenticationConverter;

    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @Test
    void getMeShouldRequireProfileScope() throws Exception {
        when(currentUserService.getCurrentUser(org.mockito.ArgumentMatchers.any())).thenReturn(
            new MeResponse("auth0|123", "User", "nick", "user@example.com", "https://example.com/p.png", List.of("read:profile"))
        );

        mockMvc.perform(get("/api/me")
                .with(jwt().jwt(jwt -> jwt.subject("auth0|123").claim("scope", "read:profile"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sub").value("auth0|123"));
    }
}
