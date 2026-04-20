package com.twittarep.backend.dto;

import java.util.List;

public record PagedResponse<T>(
    List<T> items,
    long totalItems,
    int page,
    int size
) {
}
