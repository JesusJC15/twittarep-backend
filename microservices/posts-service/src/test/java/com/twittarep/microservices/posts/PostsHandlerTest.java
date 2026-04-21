package com.twittarep.microservices.posts;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PostsHandlerTest {

    @Test
    void shouldRejectLongPost() {
        PostsHandler handler = new PostsHandler(new InMemoryPostRepository());
        APIGatewayV2HTTPEvent request = request("POST", "/api/posts", Map.of(), "{\"content\":\"" + "x".repeat(141) + "\"}");

        var response = handler.handleRequest(request, null);

        assertThat(response.getStatusCode()).isEqualTo(400);
    }

    private APIGatewayV2HTTPEvent request(String method, String path, Map<String, String> queryParams, String body) {
        APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT jwt = new APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT();
        jwt.setClaims(Map.of(
            "sub", "auth0|123",
            "name", "Jane",
            "scope", "write:posts read:profile"
        ));

        APIGatewayV2HTTPEvent.RequestContext.Authorizer authorizer = new APIGatewayV2HTTPEvent.RequestContext.Authorizer();
        authorizer.setJwt(jwt);
        APIGatewayV2HTTPEvent.RequestContext.Http http = new APIGatewayV2HTTPEvent.RequestContext.Http();
        http.setMethod(method);
        http.setPath(path);
        APIGatewayV2HTTPEvent.RequestContext ctx = new APIGatewayV2HTTPEvent.RequestContext();
        ctx.setAuthorizer(authorizer);
        ctx.setHttp(http);

        APIGatewayV2HTTPEvent request = new APIGatewayV2HTTPEvent();
        request.setRawPath(path);
        request.setBody(body);
        request.setQueryStringParameters(queryParams);
        request.setRequestContext(ctx);
        return request;
    }

    private static final class InMemoryPostRepository extends PostRepository {
        private InMemoryPostRepository() {
            super(false);
        }

        @Override
        public PostDocument save(PostDocument document) {
            document.setId("post-1");
            document.setCreatedAt(Instant.now());
            return document;
        }

        @Override
        public List<PostDocument> findPage(int page, int size) {
            return List.of();
        }

        @Override
        public long count() {
            return 0;
        }
    }
}
