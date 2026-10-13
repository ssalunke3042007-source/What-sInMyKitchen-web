CREATE DATABASE IF NOT EXISTS whats_in_my_kitchen
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE whats_in_my_kitchen;

DROP TABLE IF EXISTS recipe_ingredients;
DROP TABLE IF EXISTS recipes;
DROP TABLE IF EXISTS ingredients;
DROP TABLE IF EXISTS ingredient_categories;

CREATE TABLE ingredient_categories (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_ingredient_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ingredients (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    category_id INT NOT NULL,
    image_url VARCHAR(255) NULL,
    is_common BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_ingredient_name (name),
    KEY idx_ingredients_category_id (category_id),
    KEY idx_ingredients_name (name),
    CONSTRAINT fk_ingredients_category
        FOREIGN KEY (category_id) REFERENCES ingredient_categories(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE recipes (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    instructions TEXT NOT NULL,
    image_url VARCHAR(255) NULL,
    preparation_time_minutes INT NOT NULL,
    cooking_time_minutes INT NOT NULL,
    difficulty ENUM('Easy', 'Medium', 'Hard') NOT NULL,
    servings INT NOT NULL,
    is_vegetarian BOOLEAN NOT NULL DEFAULT TRUE,
    is_vegan BOOLEAN NOT NULL DEFAULT FALSE,
    is_egg_free BOOLEAN NOT NULL DEFAULT FALSE,
    is_dairy_free BOOLEAN NOT NULL DEFAULT FALSE,
    category VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_recipe_slug (slug),
    KEY idx_recipes_category (category),
    KEY idx_recipes_difficulty (difficulty),
    KEY idx_recipes_time (preparation_time_minutes, cooking_time_minutes)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE recipe_ingredients (
    id INT NOT NULL AUTO_INCREMENT,
    recipe_id INT NOT NULL,
    ingredient_id INT NOT NULL,
    quantity VARCHAR(50) NULL,
    unit VARCHAR(40) NULL,
    is_optional BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_recipe_ingredient_role (recipe_id, ingredient_id, is_optional),
    KEY idx_recipe_ingredients_recipe_id (recipe_id),
    KEY idx_recipe_ingredients_ingredient_id (ingredient_id),
    CONSTRAINT fk_recipe_ingredients_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipes(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_recipe_ingredients_ingredient
        FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
