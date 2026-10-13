package com.whatsinmykitchen.service;

import com.whatsinmykitchen.dao.IngredientDAO;
import com.whatsinmykitchen.model.Ingredient;
import com.whatsinmykitchen.model.IngredientCategory;
import java.sql.SQLException;
import java.util.List;

public class IngredientService {
    private final IngredientDAO ingredientDAO;

    public IngredientService() {
        this.ingredientDAO = new IngredientDAO();
    }

    public List<IngredientCategory> getCategories() throws SQLException {
        return ingredientDAO.getCategories();
    }

    public List<Ingredient> getIngredients(String search, String category, int limit, int offset) throws SQLException {
        return ingredientDAO.getIngredients(search, category, limit, offset);
    }
}
