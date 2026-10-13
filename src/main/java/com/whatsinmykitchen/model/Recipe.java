package com.whatsinmykitchen.model;

import java.util.List;

public class Recipe {
    private final int id;
    private final String name;
    private final String slug;
    private final String description;
    private final List<String> instructions;
    private final String imageUrl;
    private final int preparationTimeMinutes;
    private final int cookingTimeMinutes;
    private final String difficulty;
    private final int servings;
    private final boolean vegetarian;
    private final boolean vegan;
    private final boolean eggFree;
    private final boolean dairyFree;
    private final String category;

    public Recipe(int id, String name, String slug, String description, List<String> instructions, String imageUrl,
                  int preparationTimeMinutes, int cookingTimeMinutes, String difficulty, int servings,
                  boolean vegetarian, boolean vegan, boolean eggFree, boolean dairyFree, String category) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.instructions = instructions;
        this.imageUrl = imageUrl;
        this.preparationTimeMinutes = preparationTimeMinutes;
        this.cookingTimeMinutes = cookingTimeMinutes;
        this.difficulty = difficulty;
        this.servings = servings;
        this.vegetarian = vegetarian;
        this.vegan = vegan;
        this.eggFree = eggFree;
        this.dairyFree = dairyFree;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getInstructions() {
        return instructions;
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

    public int getServings() {
        return servings;
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

    public String getCategory() {
        return category;
    }
}
