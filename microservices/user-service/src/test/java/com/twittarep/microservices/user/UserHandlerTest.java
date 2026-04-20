package com.twittarep.microservices.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserHandlerTest {

    @Test
    void shouldReturnUserProfileFromClaims() {
        UserHandler handler = new UserHandler();
        APIGatewayProxyRequestEvent.ProxyRequestContext ctx = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        ctx.setAuthorizer(Map.of("jwt", Map.of("claims", Map.of(
            "sub", "auth0|123",
            "name", "Jane",
            "nickname", "jane",
            "scope", "read:profile"
        ))));
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setPath("/api/me");
        request.setRequestContext(ctx);

        var response = handler.handleRequest(request, null);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).contains("auth0|123");
    }
}
