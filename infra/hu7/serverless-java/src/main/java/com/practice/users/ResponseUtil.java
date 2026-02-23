package com.practice.users;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public final class ResponseUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ResponseUtil() {}

    public static APIGatewayProxyResponseEvent json(int status, Object body) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(status)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(MAPPER.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"message\":\"error serializando respuesta\"}");
        }
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }
}

