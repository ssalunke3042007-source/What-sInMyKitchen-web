package com.whatsinmykitchen.model;

public class Ingredient {
    private final int id;
    private final String name;
    private final int categoryId;
    private final String categoryName;
    private final String imageUrl;
    private final boolean common;

    public Ingredient(int id, String name, int categoryId, String categoryName, String imageUrl, boolean common) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.imageUrl = imageUrl;
        this.common = common;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isCommon() {
        return common;
    }
}
