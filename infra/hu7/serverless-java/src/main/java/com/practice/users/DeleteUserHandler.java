package com.practice.users;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Map;

public class DeleteUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String id = input.getPathParameters() != null ? input.getPathParameters().get("id") : null;
        if (id == null || id.isBlank()) {
            return ResponseUtil.json(400, Map.of("message", "id es obligatorio"));
        }
        if (!UserStore.users().containsKey(id)) {
            return ResponseUtil.json(404, Map.of("message", "Usuario no encontrado"));
        }

        UserStore.users().remove(id);
        return ResponseUtil.json(200, Map.of("message", "Usuario eliminado", "id", id));
    }
}

