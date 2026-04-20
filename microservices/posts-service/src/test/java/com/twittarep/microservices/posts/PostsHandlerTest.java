package com.twittarep.microservices.posts;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PostsHandlerTest {

    @Test
    void shouldRejectLongPost() {
        PostsHandler handler = new PostsHandler(new InMemoryPostRepository());
        APIGatewayProxyRequestEvent request = request("POST", "/api/posts", Map.of(), "{\"content\":\"" + "x".repeat(141) + "\"}");

        var response = handler.handleRequest(request, null);

        assertThat(response.getStatusCode()).isEqualTo(400);
    }

    private APIGatewayProxyRequestEvent request(String method, String path, Map<String, String> queryParams, String body) {
        APIGatewayProxyRequestEvent.ProxyRequestContext ctx = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        ctx.setAuthorizer(Map.of("jwt", Map.of("claims", Map.of(
            "sub", "auth0|123",
            "name", "Jane",
            "scope", "write:posts read:profile"
        ))));
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod(method);
        request.setPath(path);
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
