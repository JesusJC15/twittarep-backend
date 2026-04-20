package com.twittarep.shared.api;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.twittarep.shared.ObjectMapperFactory;
import com.twittarep.shared.dto.ErrorResponse;
import java.time.Instant;
import java.util.Map;

public final class ApiGatewayResponses {

    private ApiGatewayResponses() {
    }

    public static APIGatewayV2HTTPResponse ok(Object body) {
        return json(200, body);
    }

    public static APIGatewayV2HTTPResponse created(Object body) {
        return json(201, body);
    }

    public static APIGatewayV2HTTPResponse badRequest(String message, String path) {
        return json(400, new ErrorResponse(Instant.now(), 400, "Bad Request", message, path));
    }

    public static APIGatewayV2HTTPResponse unauthorized(String message, String path) {
        return json(401, new ErrorResponse(Instant.now(), 401, "Unauthorized", message, path));
    }

    public static APIGatewayV2HTTPResponse forbidden(String message, String path) {
        return json(403, new ErrorResponse(Instant.now(), 403, "Forbidden", message, path));
    }

    public static APIGatewayV2HTTPResponse serverError(String path) {
        return json(500, new ErrorResponse(Instant.now(), 500, "Internal Server Error", "Unexpected server error", path));
    }

    public static APIGatewayV2HTTPResponse json(int statusCode, Object body) {
        try {
            APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
            response.setStatusCode(statusCode);
            response.setHeaders(Map.of(
                "Content-Type", "application/json",
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Headers", "Authorization,Content-Type",
                "Access-Control-Allow-Methods", "GET,POST,OPTIONS"
            ));
            response.setBody(ObjectMapperFactory.get().writeValueAsString(body));
            return response;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize response", exception);
        }
    }
}
