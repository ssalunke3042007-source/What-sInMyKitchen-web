package com.whatsinmykitchen.controller;

import com.sun.net.httpserver.HttpExchange;
import com.whatsinmykitchen.model.Recipe;
import com.whatsinmykitchen.model.RecipeIngredient;
import com.whatsinmykitchen.model.RecipeMatch;
import com.whatsinmykitchen.service.IngredientService;
import com.whatsinmykitchen.service.RecipeService;
import com.whatsinmykitchen.util.HttpUtil;
import com.whatsinmykitchen.util.JsonUtil;
import com.whatsinmykitchen.util.ValidationUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class RecipeController {
    private final RecipeService recipeService;
    private final IngredientService ingredientService;

    public RecipeController() {
        this.recipeService = new RecipeService();
        this.ingredientService = new IngredientService();
    }

    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.startsWith("/api/recipes/match")) {
            if (!"POST".equalsIgnoreCase(method)) {
                HttpUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Only POST is supported for recipe matching.");
                return;
            }
            handleMatch(exchange);
            return;
        }

        if (path.matches("/api/recipes/\\d+")) {
            if (!"GET".equalsIgnoreCase(method)) {
                HttpUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Only GET is supported for recipe details.");
                return;
            }
            handleDetail(exchange);
            return;
        }

        if (!"GET".equalsIgnoreCase(method)) {
            HttpUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Only GET is supported for recipe listing.");
            return;
        }
        handleList(exchange);
    }

    private void handleList(HttpExchange exchange) throws IOException {
        Map<String, String> params = HttpUtil.parseQueryParams(exchange.getRequestURI());
        Map<String, Object> filters = new LinkedHashMap<>();
        String search = params.get("search");
        String category = params.get("category");
        String difficulty = params.get("difficulty");
        String vegetarian = params.get("vegetarian");
        String vegan = params.get("vegan");
        String eggFree = params.get("eggFree");
        String dairyFree = params.get("dairyFree");
        String maxTime = params.get("maxTime");
        int limit = ValidationUtil.parsePositiveInt(params.get("limit"), 20);
        int offset = ValidationUtil.parsePositiveInt(params.get("offset"), 0);

        if (limit > 200) {
            limit = 200;
        }
        if (offset < 0) {
            offset = 0;
        }

        if (!ValidationUtil.isBlank(search)) {
            filters.put("search", search);
        }
        if (!ValidationUtil.isBlank(category)) {
            filters.put("category", category);
        }
        if (!ValidationUtil.isBlank(difficulty)) {
            filters.put("difficulty", difficulty);
        }
        if (!ValidationUtil.isBlank(vegetarian)) {
            filters.put("vegetarian", Boolean.parseBoolean(vegetarian));
        }
        if (!ValidationUtil.isBlank(vegan)) {
            filters.put("vegan", Boolean.parseBoolean(vegan));
        }
        if (!ValidationUtil.isBlank(eggFree)) {
            filters.put("eggFree", Boolean.parseBoolean(eggFree));
        }
        if (!ValidationUtil.isBlank(dairyFree)) {
            filters.put("dairyFree", Boolean.parseBoolean(dairyFree));
        }
        if (!ValidationUtil.isBlank(maxTime)) {
            Integer parsed = ValidationUtil.parseIntegerOrNull(maxTime);
            if (parsed != null) {
                filters.put("maxTime", parsed);
            }
        }

        try {
            List<Recipe> recipes = recipeService.getRecipes(filters, limit, offset);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("items", recipes);
            payload.put("count", recipes.size());
            payload.put("limit", limit);
            payload.put("offset", offset);
            HttpUtil.sendJson(exchange, 200, payload);
        } catch (Exception ex) {
            HttpUtil.sendError(exchange, 500, "RECIPE_LIST_ERROR", "Unable to load recipes right now.");
        }
    }

    private void handleDetail(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        if (parts.length < 3) {
            HttpUtil.sendError(exchange, 400, "INVALID_REQUEST", "Recipe ID is missing.");
            return;
        }

        Integer recipeId = ValidationUtil.parseIntegerOrNull(parts[parts.length - 1]);
        if (recipeId == null) {
            HttpUtil.sendError(exchange, 400, "INVALID_REQUEST", "Recipe ID must be a number.");
            return;
        }

        try {
            Recipe recipe = recipeService.getRecipeById(recipeId);
            if (recipe == null) {
                HttpUtil.sendError(exchange, 404, "NOT_FOUND", "Recipe not found.");
                return;
            }

            List<RecipeIngredient> ingredients = recipeService.getRecipeIngredients(recipeId);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("recipe", recipe);
            payload.put("ingredients", ingredients);
            HttpUtil.sendJson(exchange, 200, payload);
        } catch (Exception ex) {
            HttpUtil.sendError(exchange, 500, "DETAIL_ERROR", "Unable to load recipe details right now.");
        }
    }

    private void handleMatch(HttpExchange exchange) throws IOException {
        String body = HttpUtil.readBody(exchange);
        Map<String, Object> requestBody;
        try {
            requestBody = JsonUtil.parseObject(body);
        } catch (JsonUtil.JsonParseException ex) {
            HttpUtil.sendError(exchange, 400, "INVALID_JSON", "Request body must be valid JSON.");
            return;
        }

        Object rawIngredientIds = requestBody.get("ingredientIds");
        Object rawFilters = requestBody.get("filters");

        if (!(rawIngredientIds instanceof List<?> ingredients)) {
            HttpUtil.sendError(exchange, 400, "INVALID_REQUEST", "Please supply ingredientIds as an array of numbers.");
            return;
        }
        if (ingredients.isEmpty()) {
            HttpUtil.sendError(exchange, 400, "INVALID_REQUEST", "Please select at least one valid ingredient.");
            return;
        }

        List<Integer> selectedIds = new ArrayList<>();
        for (Object rawId : ingredients) {
            if (!(rawId instanceof Number number)) {
                HttpUtil.sendError(exchange, 400, "INVALID_REQUEST", "Each ingredient ID must be a number.");
                return;
            }
            int id = number.intValue();
            if (id <= 0) {
                HttpUtil.sendError(exchange, 400, "INVALID_REQUEST", "Ingredient IDs must be greater than zero.");
                return;
            }
            selectedIds.add(id);
        }

        Set<Integer> uniqueIds = new HashSet<>(selectedIds);
        if (uniqueIds.size() > 200) {
            HttpUtil.sendError(exchange, 400, "INVALID_REQUEST", "Please select fewer than 200 ingredients.");
            return;
        }

        try {
            // Validate selected IDs against the database before calculating the recipe match.
            // This prevents invalid IDs from being accepted and keeps the algorithm honest.
            List<Integer> validIds = new ArrayList<>(new com.whatsinmykitchen.dao.IngredientDAO().validateIngredientIds(new ArrayList<>(uniqueIds)));
            if (validIds.isEmpty()) {
                HttpUtil.sendError(exchange, 400, "INVALID_INGREDIENTS", "No valid ingredient IDs were found.");
                return;
            }

            Map<String, Object> filters = new LinkedHashMap<>();
            if (rawFilters instanceof Map<?, ?> filterMap) {
                for (Map.Entry<?, ?> entry : filterMap.entrySet()) {
                    filters.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }

            List<RecipeMatch> matches = recipeService.matchRecipes(new ArrayList<>(validIds), filters);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("ingredientCount", validIds.size());
            payload.put("matches", matches);
            payload.put("count", matches.size());
            HttpUtil.sendJson(exchange, 200, payload);
        } catch (Exception ex) {
            HttpUtil.sendError(exchange, 500, "MATCH_ERROR", "Unable to match recipes right now.");
        }
    }
}
