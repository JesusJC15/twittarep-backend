package com.twittarep.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
    @NotBlank(message = "content is required")
    @Size(max = 140, message = "content must be at most 140 characters")
    String content
) {
}
