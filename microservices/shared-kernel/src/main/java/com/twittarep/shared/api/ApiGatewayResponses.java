package com.twittarep.shared.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.twittarep.shared.ObjectMapperFactory;
import com.twittarep.shared.dto.ErrorResponse;
import java.time.Instant;
import java.util.Map;

public final class ApiGatewayResponses {

    private ApiGatewayResponses() {
    }

    public static APIGatewayProxyResponseEvent ok(Object body) {
        return json(200, body);
    }

    public static APIGatewayProxyResponseEvent created(Object body) {
        return json(201, body);
    }

    public static APIGatewayProxyResponseEvent badRequest(String message, String path) {
        return json(400, new ErrorResponse(Instant.now(), 400, "Bad Request", message, path));
    }

    public static APIGatewayProxyResponseEvent unauthorized(String message, String path) {
        return json(401, new ErrorResponse(Instant.now(), 401, "Unauthorized", message, path));
    }

    public static APIGatewayProxyResponseEvent forbidden(String message, String path) {
        return json(403, new ErrorResponse(Instant.now(), 403, "Forbidden", message, path));
    }

    public static APIGatewayProxyResponseEvent serverError(String path) {
        return json(500, new ErrorResponse(Instant.now(), 500, "Internal Server Error", "Unexpected server error", path));
    }

    public static APIGatewayProxyResponseEvent json(int statusCode, Object body) {
        try {
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Map.of(
                    "Content-Type", "application/json",
                    "Access-Control-Allow-Origin", "*",
                    "Access-Control-Allow-Headers", "Authorization,Content-Type",
                    "Access-Control-Allow-Methods", "GET,POST,OPTIONS"))
                .withBody(ObjectMapperFactory.get().writeValueAsString(body));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize response", exception);
        }
    }
}
