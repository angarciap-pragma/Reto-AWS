package com.practice.users;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Map;

public class CreateUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            User user = ResponseUtil.mapper().readValue(input.getBody(), User.class);
            if (user.getId() == null || user.getId().isBlank() ||
                    user.getNombre() == null || user.getNombre().isBlank() ||
                    user.getEmail() == null || user.getEmail().isBlank()) {
                return ResponseUtil.json(400, Map.of("message", "id, nombre y email son obligatorios"));
            }
            if (UserStore.users().containsKey(user.getId())) {
                return ResponseUtil.json(409, Map.of("message", "Usuario ya existe"));
            }
            UserStore.users().put(user.getId(), user);
            return ResponseUtil.json(201, user);
        } catch (Exception e) {
            return ResponseUtil.json(400, Map.of("message", "body invalido"));
        }
    }
}

