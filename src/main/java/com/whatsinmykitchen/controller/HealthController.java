package com.whatsinmykitchen.controller;

import com.sun.net.httpserver.HttpExchange;
import com.whatsinmykitchen.config.DatabaseConfig;
import com.whatsinmykitchen.util.HttpUtil;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class HealthController {
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Only GET is supported for this endpoint.");
            return;
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("database", DatabaseConfig.isDatabaseReachable() ? "connected" : "disconnected");
        response.put("app", "What’sInMyKitchen");

        if (!DatabaseConfig.isDatabaseReachable()) {
            HttpUtil.sendJson(exchange, 503, response);
            return;
        }
        HttpUtil.sendJson(exchange, 200, response);
    }
}
