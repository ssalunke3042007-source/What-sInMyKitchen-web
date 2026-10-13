package com.whatsinmykitchen.model;

public class RecipeIngredient {
    private final int id;
    private final int recipeId;
    private final int ingredientId;
    private final String ingredientName;
    private final String quantity;
    private final String unit;
    private final boolean optional;
    private final int displayOrder;

    public RecipeIngredient(int id, int recipeId, int ingredientId, String ingredientName, String quantity, String unit, boolean optional, int displayOrder) {
        this.id = id;
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
        this.optional = optional;
        this.displayOrder = displayOrder;
    }

    public int getId() {
        return id;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isOptional() {
        return optional;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
