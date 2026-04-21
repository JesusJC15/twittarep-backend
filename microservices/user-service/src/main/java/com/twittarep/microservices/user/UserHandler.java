package com.twittarep.microservices.user;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.twittarep.shared.api.ApiGatewayResponses;
import com.twittarep.shared.auth.RequestAuthorizer;
import com.twittarep.shared.dto.MeResponse;
import java.util.List;
import java.util.Map;

public class UserHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        String path = request.getRawPath();
        if (!RequestAuthorizer.hasScope(request, "read:profile")) {
            return ApiGatewayResponses.forbidden("Missing required scope read:profile", path);
        }
        Map<String, Object> claims = RequestAuthorizer.claims(request);
        String sub = RequestAuthorizer.subject(request);
        if (sub == null || sub.isBlank()) {
            return ApiGatewayResponses.unauthorized("Missing authenticated user", path);
        }
        MeResponse response = new MeResponse(
            sub,
            stringClaim(claims, "name"),
            stringClaim(claims, "nickname"),
            stringClaim(claims, "email"),
            stringClaim(claims, "picture"),
            List.copyOf(RequestAuthorizer.scopes(request))
        );
        return ApiGatewayResponses.ok(response);
    }

    private String stringClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value == null ? null : value.toString();
    }
}
