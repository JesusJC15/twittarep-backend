package com.twittarep.shared.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RequestAuthorizerTest {

    @Test
    void shouldReadClaimsAndScopes() {
        APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT jwt = new APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT();
        jwt.setClaims(Map.of(
            "sub", "auth0|123",
            "scope", "read:profile write:posts"
        ));
        APIGatewayV2HTTPEvent.RequestContext.Authorizer authorizer = new APIGatewayV2HTTPEvent.RequestContext.Authorizer();
        authorizer.setJwt(jwt);
        APIGatewayV2HTTPEvent.RequestContext requestContext = new APIGatewayV2HTTPEvent.RequestContext();
        requestContext.setAuthorizer(authorizer);

        APIGatewayV2HTTPEvent request = new APIGatewayV2HTTPEvent();
        request.setRequestContext(requestContext);

        assertThat(RequestAuthorizer.subject(request)).isEqualTo("auth0|123");
        assertThat(RequestAuthorizer.hasScope(request, "write:posts")).isTrue();
    }
}
