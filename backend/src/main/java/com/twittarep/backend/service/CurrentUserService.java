package com.twittarep.backend.service;

import com.twittarep.backend.dto.MeResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public MeResponse getCurrentUser(Jwt jwt) {
        return new MeResponse(
            jwt.getSubject(),
            jwt.getClaimAsString("name"),
            jwt.getClaimAsString("nickname"),
            jwt.getClaimAsString("email"),
            jwt.getClaimAsString("picture"),
            extractScopes(jwt)
        );
    }

    private List<String> extractScopes(Jwt jwt) {
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || scope.isBlank()) {
            return List.of();
        }
        return Arrays.stream(scope.split(" "))
            .filter(Objects::nonNull)
            .filter(s -> !s.isBlank())
            .toList();
    }
}
