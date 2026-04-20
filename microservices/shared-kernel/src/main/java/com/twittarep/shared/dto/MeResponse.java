package com.twittarep.shared.dto;

import java.util.List;

public record MeResponse(
    String sub,
    String name,
    String nickname,
    String email,
    String picture,
    List<String> scopes
) {
}
