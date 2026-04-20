package com.twittarep.microservices.feed;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FeedHandlerTest {

    @Test
    void shouldReturnFeed() {
        FeedHandler handler = new FeedHandler(new FakeFeedRepository());
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setPath("/api/stream");
        request.setQueryStringParameters(Map.of("page", "0", "size", "20"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).contains("hello");
    }

    private static final class FakeFeedRepository extends FeedRepository {
        private FakeFeedRepository() {
            super(false);
        }

        @Override
        public List<FeedPostDocument> findPage(int page, int size) {
            FeedPostDocument post = new FeedPostDocument();
            post.setId("1");
            post.setContent("hello");
            post.setAuthorId("auth0|123");
            post.setAuthorDisplayName("Jane");
            post.setCreatedAt(Instant.parse("2026-04-19T12:00:00Z"));
            return List.of(post);
        }

        @Override
        public long count() {
            return 1;
        }
    }
}
