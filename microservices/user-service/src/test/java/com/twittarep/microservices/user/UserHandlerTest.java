package com.twittarep.microservices.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserHandlerTest {

    @Test
    void shouldReturnUserProfileFromClaims() {
        UserHandler handler = new UserHandler();
        APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT jwt = new APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT();
        jwt.setClaims(Map.of(
            "sub", "auth0|123",
            "name", "Jane",
            "nickname", "jane",
            "scope", "read:profile"
        ));
        APIGatewayV2HTTPEvent.RequestContext.Authorizer authorizer = new APIGatewayV2HTTPEvent.RequestContext.Authorizer();
        authorizer.setJwt(jwt);
        APIGatewayV2HTTPEvent.RequestContext ctx = new APIGatewayV2HTTPEvent.RequestContext();
        ctx.setAuthorizer(authorizer);

        APIGatewayV2HTTPEvent request = new APIGatewayV2HTTPEvent();
        request.setRawPath("/api/me");
        request.setRequestContext(ctx);

        var response = handler.handleRequest(request, null);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).contains("auth0|123");
    }
}
