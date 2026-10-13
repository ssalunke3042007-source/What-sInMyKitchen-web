package com.whatsinmykitchen.dao;

import com.whatsinmykitchen.config.DatabaseConfig;
import com.whatsinmykitchen.model.Recipe;
import com.whatsinmykitchen.model.RecipeIngredient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeDAO {
    public List<Recipe> findRecipes(Map<String, Object> filters, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT r.id, r.name, r.slug, r.description, r.instructions, r.image_url, r.preparation_time_minutes, " +
            "r.cooking_time_minutes, r.difficulty, r.servings, r.is_vegetarian, r.is_vegan, r.is_egg_free, " +
            "r.is_dairy_free, r.category FROM recipes r WHERE 1 = 1"
        );
        List<Object> params = new ArrayList<>();

        if (filters != null) {
            String search = (String) filters.get("search");
            String category = (String) filters.get("category");
            Boolean vegetarian = toBoolean(filters.get("vegetarian"));
            Boolean vegan = toBoolean(filters.get("vegan"));
            Boolean eggFree = toBoolean(filters.get("eggFree"));
            Boolean dairyFree = toBoolean(filters.get("dairyFree"));
            String difficulty = (String) filters.get("difficulty");
            Integer maxTime = toInteger(filters.get("maxTime"));

            if (search != null && !search.trim().isEmpty()) {
                sql.append(" AND (r.name LIKE ? OR r.description LIKE ?)");
                params.add("%" + search.trim() + "%");
                params.add("%" + search.trim() + "%");
            }
            if (category != null && !category.trim().isEmpty()) {
                sql.append(" AND r.category = ?");
                params.add(category.trim());
            }
            if (vegetarian != null) {
                sql.append(" AND r.is_vegetarian = ?");
                params.add(vegetarian ? 1 : 0);
            }
            if (vegan != null) {
                sql.append(" AND r.is_vegan = ?");
                params.add(vegan ? 1 : 0);
            }
            if (eggFree != null) {
                sql.append(" AND r.is_egg_free = ?");
                params.add(eggFree ? 1 : 0);
            }
            if (dairyFree != null) {
                sql.append(" AND r.is_dairy_free = ?");
                params.add(dairyFree ? 1 : 0);
            }
            if (maxTime != null) {
                sql.append(" AND (r.preparation_time_minutes + r.cooking_time_minutes) <= ?");
                params.add(maxTime);
            }
            if (difficulty != null && !difficulty.trim().isEmpty()) {
                sql.append(" AND r.difficulty = ?");
                params.add(difficulty.trim());
            }
        }

        sql.append(" ORDER BY r.name ASC LIMIT ? OFFSET ?");
        params.add(Math.min(Math.max(limit, 1), 200));
        params.add(Math.max(offset, 0));

        List<Recipe> recipes = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParameters(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    recipes.add(mapRecipe(resultSet));
                }
            }
        }
        return recipes;
    }

    public Recipe findRecipeById(int recipeId) throws SQLException {
        String sql = "SELECT r.id, r.name, r.slug, r.description, r.instructions, r.image_url, r.preparation_time_minutes, " +
            "r.cooking_time_minutes, r.difficulty, r.servings, r.is_vegetarian, r.is_vegan, r.is_egg_free, " +
            "r.is_dairy_free, r.category FROM recipes r WHERE r.id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, recipeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRecipe(resultSet);
                }
            }
        }
        return null;
    }

    public List<RecipeIngredient> getRecipeIngredients(int recipeId) throws SQLException {
        String sql = "SELECT ri.id, ri.recipe_id, ri.ingredient_id, i.name AS ingredient_name, ri.quantity, ri.unit, " +
            "ri.is_optional, ri.display_order FROM recipe_ingredients ri INNER JOIN ingredients i ON i.id = ri.ingredient_id " +
            "WHERE ri.recipe_id = ? ORDER BY ri.display_order ASC, ri.id ASC";

        List<RecipeIngredient> ingredients = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, recipeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ingredients.add(new RecipeIngredient(
                        resultSet.getInt("id"),
                        resultSet.getInt("recipe_id"),
                        resultSet.getInt("ingredient_id"),
                        resultSet.getString("ingredient_name"),
                        resultSet.getString("quantity"),
                        resultSet.getString("unit"),
                        resultSet.getBoolean("is_optional"),
                        resultSet.getInt("display_order")
                    ));
                }
            }
        }
        return ingredients;
    }

    private static Recipe mapRecipe(ResultSet resultSet) throws SQLException {
        String instructionsText = resultSet.getString("instructions");
        List<String> instructions = new ArrayList<>();
        if (instructionsText != null && !instructionsText.trim().isEmpty()) {
            String[] steps = instructionsText.split("\\r?\\n");
            for (String step : steps) {
                String trimmed = step.trim();
                if (!trimmed.isEmpty()) {
                    instructions.add(trimmed);
                }
            }
        }

        return new Recipe(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getString("slug"),
            resultSet.getString("description"),
            instructions,
            resultSet.getString("image_url"),
            resultSet.getInt("preparation_time_minutes"),
            resultSet.getInt("cooking_time_minutes"),
            resultSet.getString("difficulty"),
            resultSet.getInt("servings"),
            resultSet.getBoolean("is_vegetarian"),
            resultSet.getBoolean("is_vegan"),
            resultSet.getBoolean("is_egg_free"),
            resultSet.getBoolean("is_dairy_free"),
            resultSet.getString("category")
        );
    }

    private static void bindParameters(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof Integer) {
                statement.setInt(i + 1, (Integer) value);
            } else if (value instanceof Long) {
                statement.setLong(i + 1, (Long) value);
            } else if (value instanceof Boolean) {
                statement.setBoolean(i + 1, (Boolean) value);
            } else if (value instanceof Double) {
                statement.setDouble(i + 1, (Double) value);
            } else {
                statement.setString(i + 1, String.valueOf(value));
            }
        }
    }

    private static Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        String text = String.valueOf(value).trim();
        if (text.equalsIgnoreCase("true")) {
            return true;
        }
        if (text.equalsIgnoreCase("false")) {
            return false;
        }
        return null;
    }

    private static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number num) {
            return num.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
