package com.twittarep.microservices.posts;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.twittarep.shared.ObjectMapperFactory;
import com.twittarep.shared.api.ApiGatewayResponses;
import com.twittarep.shared.auth.RequestAuthorizer;
import com.twittarep.shared.dto.CreatePostRequest;
import com.twittarep.shared.dto.PagedResponse;
import com.twittarep.shared.dto.PostResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class PostsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final PostRepository repository;

    public PostsHandler() {
        this(new PostRepository());
    }

    PostsHandler(PostRepository repository) {
        this.repository = repository;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String path = request.getPath();
        String method = request.getHttpMethod();
        try {
            if ("OPTIONS".equalsIgnoreCase(method)) {
                return ApiGatewayResponses.ok(Map.of("ok", true));
            }
            if ("GET".equalsIgnoreCase(method) && "/api/posts".equals(path)) {
                return handleGet(request);
            }
            if ("POST".equalsIgnoreCase(method) && "/api/posts".equals(path)) {
                return handleCreate(request);
            }
            return ApiGatewayResponses.badRequest("Unsupported route", path);
        } catch (IllegalArgumentException exception) {
            return ApiGatewayResponses.badRequest(exception.getMessage(), path);
        } catch (Exception exception) {
            return ApiGatewayResponses.serverError(path);
        }
    }

    private APIGatewayProxyResponseEvent handleGet(APIGatewayProxyRequestEvent request) {
        int page = parsePositiveInt(request.getQueryStringParameters(), "page", 0);
        int size = parsePositiveInt(request.getQueryStringParameters(), "size", 20);
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        List<PostResponse> items = repository.findPage(page, size).stream()
            .map(this::toResponse)
            .toList();
        return ApiGatewayResponses.ok(new PagedResponse<>(items, repository.count(), page, size));
    }

    private APIGatewayProxyResponseEvent handleCreate(APIGatewayProxyRequestEvent request) throws IOException {
        if (!RequestAuthorizer.hasScope(request, "write:posts")) {
            return ApiGatewayResponses.forbidden("Missing required scope write:posts", request.getPath());
        }
        CreatePostRequest createPostRequest = ObjectMapperFactory.get().readValue(request.getBody(), CreatePostRequest.class);
        String content = createPostRequest.content() == null ? "" : createPostRequest.content().trim();
        if (content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
        if (content.length() > 140) {
            throw new IllegalArgumentException("content must be at most 140 characters");
        }
        String authorId = RequestAuthorizer.subject(request);
        if (authorId == null || authorId.isBlank()) {
            return ApiGatewayResponses.unauthorized("Missing authenticated user", request.getPath());
        }

        PostDocument document = new PostDocument();
        document.setAuthorId(authorId);
        document.setAuthorDisplayName(resolveDisplayName(request));
        document.setContent(content);
        document.setCreatedAt(Instant.now());
        return ApiGatewayResponses.created(toResponse(repository.save(document)));
    }

    private String resolveDisplayName(APIGatewayProxyRequestEvent request) {
        Object name = RequestAuthorizer.claims(request).get("name");
        if (name != null && !name.toString().isBlank()) {
            return name.toString();
        }
        Object nickname = RequestAuthorizer.claims(request).get("nickname");
        if (nickname != null && !nickname.toString().isBlank()) {
            return nickname.toString();
        }
        return RequestAuthorizer.subject(request);
    }

    private int parsePositiveInt(Map<String, String> params, String key, int defaultValue) {
        if (params == null || !params.containsKey(key)) {
            return defaultValue;
        }
        int value = Integer.parseInt(params.get(key));
        if (value < 0) {
            throw new IllegalArgumentException(key + " must be greater than or equal to 0");
        }
        return value;
    }

    private PostResponse toResponse(PostDocument document) {
        return new PostResponse(document.getId(), document.getContent(), document.getAuthorId(), document.getAuthorDisplayName(), document.getCreatedAt());
    }
}
