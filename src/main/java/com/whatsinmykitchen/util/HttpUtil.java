package com.whatsinmykitchen.util;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpUtil {
    private HttpUtil() {
    }

    public static void sendJson(HttpExchange exchange, int statusCode, Object payload) throws IOException {
        byte[] body = JsonUtil.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("X-Frame-Options", "DENY");
        exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    public static void sendText(HttpExchange exchange, int statusCode, String contentType, String bodyText) throws IOException {
        byte[] body = bodyText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    public static void sendError(HttpExchange exchange, int statusCode, String code, String message) throws IOException {
        sendJson(exchange, statusCode, ErrorResponse.of(code, message));
    }

    public static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> result = new LinkedHashMap<>();
        if (uri == null || uri.getRawQuery() == null || uri.getRawQuery().isEmpty()) {
            return result;
        }
        String[] pairs = uri.getRawQuery().split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            String key = java.net.URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            String value = keyValue.length > 1 ? java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }
}
