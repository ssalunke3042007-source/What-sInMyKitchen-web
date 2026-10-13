package com.whatsinmykitchen.model;

import java.util.List;

public class RecipeMatch {
    private final int recipeId;
    private final String name;
    private final String description;
    private final String imageUrl;
    private final int preparationTimeMinutes;
    private final int cookingTimeMinutes;
    private final String difficulty;
    private final String category;
    private final int servings;
    private final int matchPercentage;
    private final int matchedRequiredCount;
    private final int totalRequiredCount;
    private final int missingRequiredCount;
    private final List<Integer> matchedIngredientIds;
    private final List<Integer> missingIngredientIds;
    private final List<RecipeIngredient> requiredIngredients;
    private final List<RecipeIngredient> optionalIngredients;
    private final boolean vegetarian;
    private final boolean vegan;
    private final boolean eggFree;
    private final boolean dairyFree;

    public RecipeMatch(int recipeId, String name, String description, String imageUrl, int preparationTimeMinutes,
                       int cookingTimeMinutes, String difficulty, String category, int servings,
                       int matchPercentage, int matchedRequiredCount, int totalRequiredCount,
                       int missingRequiredCount, List<Integer> matchedIngredientIds,
                       List<Integer> missingIngredientIds, List<RecipeIngredient> requiredIngredients,
                       List<RecipeIngredient> optionalIngredients, boolean vegetarian, boolean vegan,
                       boolean eggFree, boolean dairyFree) {
        this.recipeId = recipeId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.preparationTimeMinutes = preparationTimeMinutes;
        this.cookingTimeMinutes = cookingTimeMinutes;
        this.difficulty = difficulty;
        this.category = category;
        this.servings = servings;
        this.matchPercentage = matchPercentage;
        this.matchedRequiredCount = matchedRequiredCount;
        this.totalRequiredCount = totalRequiredCount;
        this.missingRequiredCount = missingRequiredCount;
        this.matchedIngredientIds = matchedIngredientIds;
        this.missingIngredientIds = missingIngredientIds;
        this.requiredIngredients = requiredIngredients;
        this.optionalIngredients = optionalIngredients;
        this.vegetarian = vegetarian;
        this.vegan = vegan;
        this.eggFree = eggFree;
        this.dairyFree = dairyFree;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getPreparationTimeMinutes() {
        return preparationTimeMinutes;
    }

    public int getCookingTimeMinutes() {
        return cookingTimeMinutes;
    }

    public int getTotalTimeMinutes() {
        return preparationTimeMinutes + cookingTimeMinutes;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getCategory() {
        return category;
    }

    public int getServings() {
        return servings;
    }

    public int getMatchPercentage() {
        return matchPercentage;
    }

    public int getMatchedRequiredCount() {
        return matchedRequiredCount;
    }

    public int getTotalRequiredCount() {
        return totalRequiredCount;
    }

    public int getMissingRequiredCount() {
        return missingRequiredCount;
    }

    public List<Integer> getMatchedIngredientIds() {
        return matchedIngredientIds;
    }

    public List<Integer> getMissingIngredientIds() {
        return missingIngredientIds;
    }

    public List<RecipeIngredient> getRequiredIngredients() {
        return requiredIngredients;
    }

    public List<RecipeIngredient> getOptionalIngredients() {
        return optionalIngredients;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public boolean isVegan() {
        return vegan;
    }

    public boolean isEggFree() {
        return eggFree;
    }

    public boolean isDairyFree() {
        return dairyFree;
    }
}
