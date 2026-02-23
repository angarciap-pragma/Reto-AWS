package com.practice.users;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Map;

public class UpdateUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String id = input.getPathParameters() != null ? input.getPathParameters().get("id") : null;
        if (id == null || id.isBlank()) {
            return ResponseUtil.json(400, Map.of("message", "id es obligatorio"));
        }
        if (!UserStore.users().containsKey(id)) {
            return ResponseUtil.json(404, Map.of("message", "Usuario no encontrado"));
        }

        try {
            User body = ResponseUtil.mapper().readValue(input.getBody(), User.class);
            if (body.getNombre() == null || body.getNombre().isBlank() ||
                    body.getEmail() == null || body.getEmail().isBlank()) {
                return ResponseUtil.json(400, Map.of("message", "nombre y email son obligatorios"));
            }
            User updated = new User(id, body.getNombre(), body.getEmail());
            UserStore.users().put(id, updated);
            return ResponseUtil.json(200, updated);
        } catch (Exception e) {
            return ResponseUtil.json(400, Map.of("message", "body invalido"));
        }
    }
}

