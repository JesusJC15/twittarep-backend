package com.twittarep.microservices.feed;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.twittarep.shared.api.ApiGatewayResponses;
import com.twittarep.shared.dto.PagedResponse;
import com.twittarep.shared.dto.PostResponse;
import java.util.List;

public class FeedHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final FeedRepository repository;

    public FeedHandler() {
        this(new FeedRepository());
    }

    FeedHandler(FeedRepository repository) {
        this.repository = repository;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            int page = parseInt(request.getQueryStringParameters() == null ? null : request.getQueryStringParameters().get("page"), 0);
            int size = parseInt(request.getQueryStringParameters() == null ? null : request.getQueryStringParameters().get("size"), 20);
            if (size < 1 || size > 100) {
                throw new IllegalArgumentException("size must be between 1 and 100");
            }
            List<PostResponse> items = repository.findPage(page, size).stream()
                .map(this::toResponse)
                .toList();
            return ApiGatewayResponses.ok(new PagedResponse<>(items, repository.count(), page, size));
        } catch (IllegalArgumentException exception) {
            return ApiGatewayResponses.badRequest(exception.getMessage(), request.getPath());
        } catch (Exception exception) {
            return ApiGatewayResponses.serverError(request.getPath());
        }
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        int parsed = Integer.parseInt(value);
        if (parsed < 0) {
            throw new IllegalArgumentException("pagination values must be greater than or equal to 0");
        }
        return parsed;
    }

    private PostResponse toResponse(FeedPostDocument document) {
        return new PostResponse(
            document.getId(),
            document.getContent(),
            document.getAuthorId(),
            document.getAuthorDisplayName(),
            document.getCreatedAt()
        );
    }
}
