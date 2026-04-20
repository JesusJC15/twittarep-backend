package com.twittarep.shared.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RequestAuthorizerTest {

    @Test
    void shouldReadClaimsAndScopes() {
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        requestContext.setAuthorizer(Map.of("jwt", Map.of("claims", Map.of(
            "sub", "auth0|123",
            "scope", "read:profile write:posts"
        ))));

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setRequestContext(requestContext);

        assertThat(RequestAuthorizer.subject(request)).isEqualTo("auth0|123");
        assertThat(RequestAuthorizer.hasScope(request, "write:posts")).isTrue();
    }
}
