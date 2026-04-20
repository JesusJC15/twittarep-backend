package com.twittarep.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.twittarep.backend.dto.CreatePostRequest;
import com.twittarep.backend.model.PostDocument;
import com.twittarep.backend.repository.PostRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void shouldCreateValidPost() {
        when(postRepository.save(any(PostDocument.class))).thenAnswer(invocation -> {
            PostDocument post = invocation.getArgument(0);
            post.setId("post-1");
            return post;
        });

        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("auth0|123")
            .claim("name", "Jane")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();

        var response = postService.createPost(jwt, new CreatePostRequest("hello"));

        assertThat(response.id()).isEqualTo("post-1");
        assertThat(response.authorDisplayName()).isEqualTo("Jane");
    }

    @Test
    void shouldRejectTooLongPost() {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("auth0|123")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();

        assertThatThrownBy(() -> postService.createPost(jwt, new CreatePostRequest("x".repeat(141))))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("140");
    }
}
