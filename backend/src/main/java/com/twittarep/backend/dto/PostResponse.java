package com.twittarep.backend.dto;

import java.time.Instant;

public record PostResponse(
    String id,
    String content,
    String authorId,
    String authorDisplayName,
    Instant createdAt
) {
}
