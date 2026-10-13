import { loadIngredientsAndCategories, renderCategoryOptions, renderIngredientList } from './ingredients.js';
import { fetchRecipeMatch, renderRecipeCards, fetchRecipeDetail, renderRecipeDetail } from './recipes.js';
import { getFavorites, toggleFavorite, isFavorite } from './favourites.js';
import { showToast, renderEmptyState, hideModal, showModal, setLoading } from './ui.js';

const state = {
  ingredientMap: new Map(),
  selectedIds: new Set(),
  ingredients: [],
  categories: [],
  recipes: [],
  favorites: getFavorites()
};

document.addEventListener('DOMContentLoaded', () => {
  initializeUi();
  loadInitialData();
});

function initializeUi() {
  document.getElementById('ingredient-search').addEventListener('input', () => renderIngredientListForFilter());
  document.getElementById('category-filter').addEventListener('change', () => renderIngredientListForFilter());
  document.getElementById('clear-selection').addEventListener('click', clearSelection);
  document.getElementById('find-recipes').addEventListener('click', findRecipes);
  document.getElementById('mobile-menu-toggle').addEventListener('click', () => {
    const nav = document.querySelector('.main-nav');
    nav.classList.toggle('open');
  });

  document.getElementById('ingredient-list').addEventListener('click', (event) => {
    const button = event.target.closest('[data-ingredient-id]');
    if (!button) return;
    const ingredientId = Number(button.dataset.ingredientId);
    toggleSelectedIngredient(ingredientId);
  });

  document.getElementById('selected-chips').addEventListener('click', (event) => {
    const button = event.target.closest('[data-remove-id]');
    if (!button) return;
    toggleSelectedIngredient(Number(button.dataset.removeId));
  });

  document.getElementById('results-sort').addEventListener('change', () => renderRecipes());

  const filterInputs = document.querySelectorAll('#recipe-filters input, #recipe-filters select');
  filterInputs.forEach(input => input.addEventListener('change', () => {
    if (state.selectedIds.size > 0) {
      findRecipes();
    }
  }));

  document.querySelectorAll('[data-close-modal]').forEach(element => {
    element.addEventListener('click', hideModal);
  });

  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape') {
      hideModal();
    }
  });

  renderFavoriteList();
}

async function loadInitialData() {
  try {
    const { ingredients, categories } = await loadIngredientsAndCategories();
    state.ingredients = ingredients;
    state.categories = categories;
    state.ingredientMap = new Map(ingredients.map(item => [Number(item.id), item]));
    renderCategoryOptions(categories);
    renderIngredientListForFilter();
    updateSelectedChipsUi();
    renderFavoriteList();
  } catch (error) {
    showToast(error.message || 'Unable to load ingredients.', 'error');
    const container = document.getElementById('ingredient-list');
    renderEmptyState(container, 'Ingredients unavailable', 'The backend is currently unavailable. Please try again later.');
  }
}

function renderIngredientListForFilter() {
  const search = document.getElementById('ingredient-search').value.trim().toLowerCase();
  const category = document.getElementById('category-filter').value;
  const visible = state.ingredients.filter((ingredient) => {
    const matchesSearch = ingredient.name.toLowerCase().includes(search);
    const matchesCategory = category ? ingredient.categoryName === category : true;
    return matchesSearch && matchesCategory;
  });
  renderIngredientList(visible, state.selectedIds);
}

function toggleSelectedIngredient(ingredientId) {
  if (state.selectedIds.has(ingredientId)) {
    state.selectedIds.delete(ingredientId);
  } else {
    state.selectedIds.add(ingredientId);
  }
  updateSelectedChipsUi();
  renderIngredientListForFilter();
}

function clearSelection() {
  state.selectedIds.clear();
  updateSelectedChipsUi();
  renderIngredientListForFilter();
}

function updateSelectedChipsUi() {
  const selected = state.ingredients.filter(item => state.selectedIds.has(Number(item.id)));
  const container = document.getElementById('selected-chips');
  const counter = document.getElementById('selection-counter');
  if (!container || !counter) return;

  if (!selected.length) {
    container.innerHTML = '<span class="empty-state" style="width:100%;padding:12px 10px;">No ingredients selected yet.</span>';
    counter.textContent = '0 selected';
    return;
  }

  container.innerHTML = selected.map(item => `
    <span class="selected-chip">
      <span>${escapeHtml(item.name)}</span>
      <button type="button" data-remove-id="${item.id}" aria-label="Remove ${escapeHtml(item.name)}">×</button>
    </span>
  `).join('');
  counter.textContent = `${selected.length} selected`;
}

function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

async function findRecipes() {
  if (!state.selectedIds.size) {
    showToast('Please select at least one ingredient.', 'error');
    return;
  }

  const button = document.getElementById('find-recipes');
  setLoading(button, true, 'Finding recipes...');

  try {
    const filters = readCurrentFilters();
    const recipes = await fetchRecipeMatch(Array.from(state.selectedIds), filters);
    state.recipes = recipes;
    renderRecipes();
    showToast(`${recipes.length} recipe match${recipes.length === 1 ? '' : 'es'} found.`, 'success');
  } catch (error) {
    showToast(error.message || 'Unable to find recipes.', 'error');
    const results = document.getElementById('recipe-results');
    renderEmptyState(results, 'No recipe results available', error.message || 'Please check your selections and try again.');
  } finally {
    setLoading(button, false, 'Find My Recipes');
  }
}

function readCurrentFilters() {
  const filters = {};
  const vegetarian = document.getElementById('filter-vegetarian');
  const vegan = document.getElementById('filter-vegan');
  const eggFree = document.getElementById('filter-egg-free');
  const dairyFree = document.getElementById('filter-dairy-free');
  const maxTime = document.getElementById('filter-max-time');
  const difficulty = document.getElementById('filter-difficulty');
  const minimumMatch = document.getElementById('filter-min-match');

  if (vegetarian.checked) filters.vegetarian = true;
  if (vegan.checked) filters.vegan = true;
  if (eggFree.checked) filters.eggFree = true;
  if (dairyFree.checked) filters.dairyFree = true;
  if (maxTime.value) filters.maxTime = Number(maxTime.value);
  if (difficulty.value) filters.difficulty = difficulty.value;
  if (minimumMatch.value) filters.minimumMatch = Number(minimumMatch.value);
  return filters;
}

function renderRecipes() {
  const sortValue = document.getElementById('results-sort').value;
  const recipes = [...state.recipes];
  recipes.sort((a, b) => {
    if (sortValue === 'shortest-time') {
      return (a.preparationTimeMinutes + a.cookingTimeMinutes) - (b.preparationTimeMinutes + b.cookingTimeMinutes);
    }
    if (sortValue === 'alphabetical') {
      return a.name.localeCompare(b.name);
    }
    return (b.matchPercentage || 0) - (a.matchPercentage || 0)
      || (a.missingRequiredCount || 0) - (b.missingRequiredCount || 0)
      || (a.preparationTimeMinutes + a.cookingTimeMinutes) - (b.preparationTimeMinutes + b.cookingTimeMinutes);
  });

  renderRecipeCards(recipes, state.favorites, openRecipeDetail, toggleFavoriteCard);
}

async function openRecipeDetail(recipeId) {
  try {
    const data = await fetchRecipeDetail(recipeId);
    renderRecipeDetail(data);
    showModal();
  } catch (error) {
    showToast(error.message || 'Unable to load recipe details.', 'error');
  }
}

function toggleFavoriteCard(recipeId) {
  const isNewFavorite = toggleFavorite(recipeId);
  state.favorites = getFavorites();
  renderRecipes();
  renderFavoriteList();
  showToast(isNewFavorite ? 'Added to favourites.' : 'Removed from favourites.', isNewFavorite ? 'success' : 'info');
}

function renderFavoriteList() {
  const container = document.getElementById('favorites-list');
  if (!container) return;

  const favorites = state.favorites;
  if (!favorites.length) {
    renderEmptyState(container, 'No favourites yet', 'Favourite recipes you love to keep them handy for later.');
    return;
  }

  const favoriteRecipes = state.recipes.filter(recipe => favorites.includes(Number(recipe.recipeId || recipe.id)));
  if (!favoriteRecipes.length) {
    container.innerHTML = favouritesFallbackHtml(favorites);
    return;
  }

  container.innerHTML = favoriteRecipes.map(recipe => `
    <article class="mini-card">
      <span class="chip">Saved</span>
      <h3>${escapeHtml(recipe.name)}</h3>
      <p>${escapeHtml(recipe.description || 'A favourite recipe to revisit later.')}</p>
      <button class="secondary-btn" type="button" data-open-detail="${recipe.recipeId || recipe.id}">Open</button>
    </article>
  `).join('');

  container.querySelectorAll('[data-open-detail]').forEach(button => {
    button.addEventListener('click', () => openRecipeDetail(Number(button.dataset.openDetail)));
  });
}

function favouritesFallbackHtml(favorites) {
  return `
    <div class="empty-state">
      <h3>${favorites.length} saved recipe${favorites.length === 1 ? '' : 's'}</h3>
      <p>Save a few recipes from the results to build your personal shortlist.</p>
    </div>
  `;
}

window.addEventListener('load', () => {
  const nav = document.querySelector('.main-nav');
  if (nav) {
    nav.classList.remove('open');
  }
});
