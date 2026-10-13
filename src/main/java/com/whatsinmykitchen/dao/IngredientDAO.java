package com.whatsinmykitchen.dao;

import com.whatsinmykitchen.config.DatabaseConfig;
import com.whatsinmykitchen.model.Ingredient;
import com.whatsinmykitchen.model.IngredientCategory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IngredientDAO {
    public List<IngredientCategory> getCategories() throws SQLException {
        String sql = "SELECT id, name, description FROM ingredient_categories ORDER BY name ASC";
        List<IngredientCategory> categories = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(new IngredientCategory(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description")
                ));
            }
        }
        return categories;
    }

    public List<Ingredient> getIngredients(String search, String category, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT i.id, i.name, i.category_id, c.name AS category_name, i.image_url, i.is_common " +
            "FROM ingredients i INNER JOIN ingredient_categories c ON c.id = i.category_id WHERE 1 = 1"
        );
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND i.name LIKE ?");
            params.add("%" + search.trim() + "%");
        }
        if (category != null && !category.trim().isEmpty()) {
            sql.append(" AND c.name = ?");
            params.add(category.trim());
        }
        sql.append(" ORDER BY i.name ASC LIMIT ? OFFSET ?");
        params.add(Math.min(limit, 200));
        params.add(offset);

        List<Ingredient> ingredients = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object value = params.get(i);
                if (value instanceof Integer) {
                    statement.setInt(i + 1, (Integer) value);
                } else if (value instanceof Long) {
                    statement.setLong(i + 1, (Long) value);
                } else {
                    statement.setString(i + 1, String.valueOf(value));
                }
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ingredients.add(new Ingredient(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("category_id"),
                        resultSet.getString("category_name"),
                        resultSet.getString("image_url"),
                        resultSet.getBoolean("is_common")
                    ));
                }
            }
        }
        return ingredients;
    }

    public Set<Integer> validateIngredientIds(List<Integer> selectedIds) throws SQLException {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Integer> deduplicated = new HashSet<>();
        deduplicated.addAll(selectedIds);
        if (deduplicated.isEmpty()) {
            return Collections.emptySet();
        }

        StringBuilder sql = new StringBuilder("SELECT id FROM ingredients WHERE id IN (");
        for (int i = 0; i < deduplicated.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");

        List<Integer> validIds = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            for (Integer id : deduplicated) {
                statement.setInt(index, id);
                index++;
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    validIds.add(resultSet.getInt("id"));
                }
            }
        }
        return new HashSet<>(validIds);
    }
}
