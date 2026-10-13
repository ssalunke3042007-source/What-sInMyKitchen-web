package com.whatsinmykitchen.server;

import com.sun.net.httpserver.HttpServer;
import com.whatsinmykitchen.controller.HealthController;
import com.whatsinmykitchen.controller.IngredientController;
import com.whatsinmykitchen.controller.RecipeController;

public final class Router {
    private Router() {
    }

    public static void registerRoutes(HttpServer server) {
        HealthController healthController = new HealthController();
        IngredientController ingredientController = new IngredientController();
        RecipeController recipeController = new RecipeController();

        server.createContext("/api/health", exchange -> {
            try {
                healthController.handle(exchange);
            } catch (Exception ex) {
                try {
                    com.whatsinmykitchen.util.HttpUtil.sendError(exchange, 500, "SERVER_ERROR", "Unexpected server error.");
                } catch (Exception ignored) {
                }
            }
        });

        server.createContext("/api/ingredients", exchange -> {
            try {
                ingredientController.handle(exchange);
            } catch (Exception ex) {
                try {
                    com.whatsinmykitchen.util.HttpUtil.sendError(exchange, 500, "SERVER_ERROR", "Unexpected server error.");
                } catch (Exception ignored) {
                }
            }
        });
        server.createContext("/api/categories", exchange -> {
            try {
                ingredientController.handle(exchange);
            } catch (Exception ex) {
                try {
                    com.whatsinmykitchen.util.HttpUtil.sendError(exchange, 500, "SERVER_ERROR", "Unexpected server error.");
                } catch (Exception ignored) {
                }
            }
        });
        server.createContext("/api/recipes", exchange -> {
            try {
                recipeController.handle(exchange);
            } catch (Exception ex) {
                try {
                    com.whatsinmykitchen.util.HttpUtil.sendError(exchange, 500, "SERVER_ERROR", "Unexpected server error.");
                } catch (Exception ignored) {
                }
            }
        });

        server.createContext("/", new StaticFileHandler("public"));
    }
}
