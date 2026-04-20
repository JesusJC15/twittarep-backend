package com.twittarep.shared.auth;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class RequestAuthorizer {

    private RequestAuthorizer() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> claims(APIGatewayProxyRequestEvent request) {
        if (request.getRequestContext() == null || request.getRequestContext().getAuthorizer() == null) {
            return Collections.emptyMap();
        }
        Object jwtObject = request.getRequestContext().getAuthorizer().get("jwt");
        if (!(jwtObject instanceof Map<?, ?> jwtMap)) {
            return Collections.emptyMap();
        }
        Object claimsObject = jwtMap.get("claims");
        if (!(claimsObject instanceof Map<?, ?> claimMap)) {
            return Collections.emptyMap();
        }
        return (Map<String, Object>) claimMap;
    }

    public static String subject(APIGatewayProxyRequestEvent request) {
        Object sub = claims(request).get("sub");
        return sub == null ? null : sub.toString();
    }

    public static List<String> scopes(APIGatewayProxyRequestEvent request) {
        Object scope = claims(request).get("scope");
        if (scope == null || scope.toString().isBlank()) {
            return List.of();
        }
        return List.of(scope.toString().split(" "));
    }

    public static boolean hasScope(APIGatewayProxyRequestEvent request, String expectedScope) {
        return scopes(request).stream().anyMatch(expectedScope::equals);
    }
}
