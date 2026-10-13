package com.whatsinmykitchen.service;

import com.whatsinmykitchen.dao.RecipeDAO;
import com.whatsinmykitchen.model.Recipe;
import com.whatsinmykitchen.model.RecipeIngredient;
import com.whatsinmykitchen.model.RecipeMatch;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecipeMatchingService {
    private final RecipeDAO recipeDAO;

    public RecipeMatchingService() {
        this.recipeDAO = new RecipeDAO();
    }

    public List<RecipeMatch> matchRecipes(List<Integer> ingredientIds, Map<String, Object> filters) throws SQLException {
        if (ingredientIds == null || ingredientIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> selected = new HashSet<>(ingredientIds);
        Map<String, Object> candidateFilters = filters == null ? Map.of() : filters;
        List<Recipe> recipes = recipeDAO.findRecipes(candidateFilters, 200, 0);

        List<RecipeMatch> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            List<RecipeIngredient> allIngredients = recipeDAO.getRecipeIngredients(recipe.getId());
            List<RecipeIngredient> requiredIngredients = new ArrayList<>();
            List<RecipeIngredient> optionalIngredients = new ArrayList<>();
            for (RecipeIngredient ingredient : allIngredients) {
                if (ingredient.isOptional()) {
                    optionalIngredients.add(ingredient);
                } else {
                    requiredIngredients.add(ingredient);
                }
            }

            List<Integer> matchedIngredientIds = new ArrayList<>();
            Set<Integer> requiredIds = new HashSet<>();
            for (RecipeIngredient ingredient : requiredIngredients) {
                requiredIds.add(ingredient.getIngredientId());
                if (selected.contains(ingredient.getIngredientId())) {
                    matchedIngredientIds.add(ingredient.getIngredientId());
                }
            }

            // Match formula: (matched required ingredients / total required ingredients) * 100.
            // Optional ingredients are excluded from the denominator and never increase the score.
            int totalRequired = requiredIngredients.size();
            int matchedRequired = matchedIngredientIds.size();
            int matchPercentage = totalRequired == 0 ? 0 : (int) Math.round((matchedRequired * 100.0) / totalRequired);
            List<Integer> missingIngredientIds = new ArrayList<>();
            for (RecipeIngredient ingredient : requiredIngredients) {
                if (!selected.contains(ingredient.getIngredientId())) {
                    missingIngredientIds.add(ingredient.getIngredientId());
                }
            }

            int minimumMatch = filters != null && filters.containsKey("minimumMatch")
                ? toInteger(filters.get("minimumMatch"))
                : 0;
            if (matchPercentage < minimumMatch) {
                continue;
            }

            matches.add(new RecipeMatch(
                recipe.getId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getImageUrl(),
                recipe.getPreparationTimeMinutes(),
                recipe.getCookingTimeMinutes(),
                recipe.getDifficulty(),
                recipe.getCategory(),
                recipe.getServings(),
                matchPercentage,
                matchedRequired,
                totalRequired,
                missingIngredientIds.size(),
                matchedIngredientIds,
                missingIngredientIds,
                requiredIngredients,
                optionalIngredients,
                recipe.isVegetarian(),
                recipe.isVegan(),
                recipe.isEggFree(),
                recipe.isDairyFree()
            ));
        }

        matches.sort(Comparator
            .comparingInt((RecipeMatch match) -> -match.getMatchPercentage())
            .thenComparingInt(match -> match.getMissingRequiredCount())
            .thenComparingInt(match -> match.getTotalTimeMinutes())
            .thenComparing(match -> match.getName()));

        return matches;
    }

    private static int toInteger(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
