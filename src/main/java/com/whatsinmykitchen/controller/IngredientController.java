package com.whatsinmykitchen.controller;

import com.sun.net.httpserver.HttpExchange;
import com.whatsinmykitchen.model.Ingredient;
import com.whatsinmykitchen.model.IngredientCategory;
import com.whatsinmykitchen.service.IngredientService;
import com.whatsinmykitchen.util.HttpUtil;
import com.whatsinmykitchen.util.ValidationUtil;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IngredientController {
    private final IngredientService ingredientService;

    public IngredientController() {
        this.ingredientService = new IngredientService();
    }

    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();

        if (!"GET".equalsIgnoreCase(method)) {
            HttpUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Only GET is supported for ingredients.");
            return;
        }

        if (uri.getPath().endsWith("/categories")) {
            handleCategories(exchange);
            return;
        }

        handleIngredients(exchange);
    }

    private void handleIngredients(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = HttpUtil.parseQueryParams(exchange.getRequestURI());
        String search = queryParams.getOrDefault("search", "");
        String category = queryParams.getOrDefault("category", "");
        int limit = ValidationUtil.parsePositiveInt(queryParams.get("limit"), 100);
        int offset = ValidationUtil.parsePositiveInt(queryParams.get("offset"), 0);

        if (limit > 200) {
            limit = 200;
        }

        try {
            List<Ingredient> ingredients = ingredientService.getIngredients(search, category, limit, offset);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("items", ingredients);
            payload.put("count", ingredients.size());
            payload.put("limit", limit);
            payload.put("offset", offset);
            HttpUtil.sendJson(exchange, 200, payload);
        } catch (Exception ex) {
            HttpUtil.sendError(exchange, 500, "INGREDIENTS_ERROR", "Unable to load ingredients right now.");
        }
    }

    private void handleCategories(HttpExchange exchange) throws IOException {
        try {
            List<IngredientCategory> categories = ingredientService.getCategories();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("items", categories);
            payload.put("count", categories.size());
            HttpUtil.sendJson(exchange, 200, payload);
        } catch (Exception ex) {
            HttpUtil.sendError(exchange, 500, "CATEGORY_ERROR", "Unable to load ingredient categories.");
        }
    }
}
