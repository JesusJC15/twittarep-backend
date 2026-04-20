package com.twittarep.backend.service;

import com.twittarep.backend.dto.CreatePostRequest;
import com.twittarep.backend.dto.PagedResponse;
import com.twittarep.backend.dto.PostResponse;
import com.twittarep.backend.model.PostDocument;
import com.twittarep.backend.repository.PostRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PagedResponse<PostResponse> getPosts(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var postPage = postRepository.findAll(pageable);
        List<PostResponse> items = postPage.getContent().stream()
            .map(this::toResponse)
            .toList();
        return new PagedResponse<>(items, postPage.getTotalElements(), page, size);
    }

    public PostResponse createPost(Jwt jwt, CreatePostRequest request) {
        String trimmedContent = request.content().trim();
        if (trimmedContent.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (trimmedContent.length() > 140) {
            throw new IllegalArgumentException("content must be at most 140 characters");
        }

        PostDocument post = new PostDocument();
        post.setAuthorId(jwt.getSubject());
        post.setAuthorDisplayName(resolveDisplayName(jwt));
        post.setContent(trimmedContent);
        post.setCreatedAt(Instant.now());
        return toResponse(postRepository.save(post));
    }

    private String resolveDisplayName(Jwt jwt) {
        String name = jwt.getClaimAsString("name");
        if (name != null && !name.isBlank()) {
            return name;
        }
        String nickname = jwt.getClaimAsString("nickname");
        if (nickname != null && !nickname.isBlank()) {
            return nickname;
        }
        return jwt.getSubject();
    }

    private PostResponse toResponse(PostDocument post) {
        return new PostResponse(post.getId(), post.getContent(), post.getAuthorId(), post.getAuthorDisplayName(), post.getCreatedAt());
    }
}
