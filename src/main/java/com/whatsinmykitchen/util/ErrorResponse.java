package com.whatsinmykitchen.util;

import java.util.Map;

public final class ErrorResponse {
    private ErrorResponse() {
    }

    public static Map<String, Object> of(String code, String message) {
        return Map.of(
            "error", Map.of(
                "code", code,
                "message", message
            )
        );
    }
}
