package com.twittarep.shared.auth;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class RequestAuthorizer {

    private RequestAuthorizer() {
    }

    public static Map<String, Object> claims(APIGatewayV2HTTPEvent request) {
        if (request == null || request.getRequestContext() == null || request.getRequestContext().getAuthorizer() == null) {
            return Collections.emptyMap();
        }
        APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT jwt = request.getRequestContext().getAuthorizer().getJwt();
        if (jwt == null || jwt.getClaims() == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(jwt.getClaims());
    }

    public static String subject(APIGatewayV2HTTPEvent request) {
        Object sub = claims(request).get("sub");
        return sub == null ? null : sub.toString();
    }

    public static List<String> scopes(APIGatewayV2HTTPEvent request) {
        Object scope = claims(request).get("scope");
        if (scope == null || scope.toString().isBlank()) {
            return List.of();
        }
        return List.of(scope.toString().split(" "));
    }

    public static boolean hasScope(APIGatewayV2HTTPEvent request, String expectedScope) {
        return scopes(request).stream().anyMatch(expectedScope::equals);
    }
}
