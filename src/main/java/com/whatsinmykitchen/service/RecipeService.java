package com.whatsinmykitchen.service;

import com.whatsinmykitchen.dao.RecipeDAO;
import com.whatsinmykitchen.model.Recipe;
import com.whatsinmykitchen.model.RecipeIngredient;
import com.whatsinmykitchen.model.RecipeMatch;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecipeService {
    private final RecipeDAO recipeDAO;
    private final RecipeMatchingService recipeMatchingService;

    public RecipeService() {
        this.recipeDAO = new RecipeDAO();
        this.recipeMatchingService = new RecipeMatchingService();
    }

    public List<Recipe> getRecipes(Map<String, Object> filters, int limit, int offset) throws SQLException {
        return recipeDAO.findRecipes(filters, limit, offset);
    }

    public Recipe getRecipeById(int id) throws SQLException {
        return recipeDAO.findRecipeById(id);
    }

    public List<RecipeIngredient> getRecipeIngredients(int recipeId) throws SQLException {
        return recipeDAO.getRecipeIngredients(recipeId);
    }

    public List<RecipeMatch> matchRecipes(List<Integer> ingredientIds, Map<String, Object> filters) throws SQLException {
        return recipeMatchingService.matchRecipes(ingredientIds, filters);
    }

    public RecipeMatch buildRecipeMatch(Recipe recipe, Set<Integer> selectedIngredientIds) throws SQLException {
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
        List<Integer> missingIngredientIds = new ArrayList<>();
        for (RecipeIngredient ingredient : requiredIngredients) {
            if (selectedIngredientIds.contains(ingredient.getIngredientId())) {
                matchedIngredientIds.add(ingredient.getIngredientId());
            } else {
                missingIngredientIds.add(ingredient.getIngredientId());
            }
        }

        int totalRequired = requiredIngredients.size();
        int matchPercentage = totalRequired == 0 ? 0 : (int) Math.round((matchedIngredientIds.size() * 100.0) / totalRequired);

        return new RecipeMatch(
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
            matchedIngredientIds.size(),
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
        );
    }
}
