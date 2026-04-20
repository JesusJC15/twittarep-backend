package com.twittarep.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twittarep.backend.dto.PagedResponse;
import com.twittarep.backend.dto.PostResponse;
import com.twittarep.backend.service.PostService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostController.class)
@Import(com.twittarep.backend.config.SecurityConfig.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private com.twittarep.backend.security.CustomJwtAuthenticationConverter jwtAuthenticationConverter;

    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @Test
    void getPostsShouldBePublic() throws Exception {
        when(postService.getPosts(0, 20)).thenReturn(new PagedResponse<>(
            List.of(new PostResponse("1", "hello", "user-1", "User One", Instant.parse("2026-04-19T12:00:00Z"))),
            1,
            0,
            20
        ));

        mockMvc.perform(get("/api/posts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].content").value("hello"));
    }

    @Test
    void createPostShouldRequireWriteScope() throws Exception {
        when(postService.createPost(any(), any())).thenReturn(new PostResponse("1", "hello", "auth0|123", "User One", Instant.now()));

        mockMvc.perform(post("/api/posts")
                .with(jwt().jwt(jwt -> jwt.subject("auth0|123").claim("scope", "write:posts")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("content", "hello"))))
            .andExpect(status().isCreated());
    }

    @Test
    void createPostShouldRejectWithoutScope() throws Exception {
        mockMvc.perform(post("/api/posts")
                .with(jwt().jwt(jwt -> jwt.subject("auth0|123").claim("scope", "read:profile")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("content", "hello"))))
            .andExpect(status().isForbidden());
    }
}
