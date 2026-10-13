import { requestJson, buildQueryString } from './api.js';
import { showToast, renderEmptyState, escapeHtml } from './ui.js';
import { isFavorite, toggleFavorite } from './favourites.js';

export async function fetchRecipes(filters = {}) {
  const query = buildQueryString(filters);
  const data = await requestJson(`/api/recipes${query}`);
  return Array.isArray(data && data.items) ? data.items : [];
}

export async function fetchRecipeMatch(selectedIds, filters = {}) {
  const payload = {
    ingredientIds: selectedIds,
    filters: filters
  };
  const data = await requestJson('/api/recipes/match', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  return Array.isArray(data && data.matches) ? data.matches : [];
}

export async function fetchRecipeDetail(recipeId) {
  const data = await requestJson(`/api/recipes/${recipeId}`);
  return data || {};
}

export function renderRecipeCards(recipes, favoriteIds, onOpen, onToggleFavorite) {
  const container = document.getElementById('recipe-results');
  if (!container) return;

  if (!recipes.length) {
    renderEmptyState(container, 'No matching recipes found', 'Try selecting more ingredients or widening your filters.');
    return;
  }

  container.innerHTML = recipes.map(recipe => {
    const isFav = favoriteIds.includes(Number(recipe.recipeId ?? recipe.id));
    const id = Number(recipe.recipeId ?? recipe.id);
    const image = recipe.imageUrl || 'https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=900&q=80';
    const desc = recipe.description || 'A delicious meal idea built from your current ingredients.';
    const meta = `
      <span class="meta-pill">${recipe.preparationTimeMinutes + recipe.cookingTimeMinutes} min</span>
      <span class="meta-pill">${recipe.difficulty || 'Easy'}</span>
      <span class="meta-pill">${recipe.category || 'General'}</span>
    `;
    const dietary = [
      recipe.vegetarian ? 'Vegetarian' : null,
      recipe.vegan ? 'Vegan' : null,
      recipe.eggFree ? 'Egg-free' : null,
      recipe.dairyFree ? 'Dairy-free' : null
    ].filter(Boolean).map(tag => `<span class="dietary-tag">${tag}</span>`).join('') || '<span class="dietary-tag">Classic</span>';

    return `
      <article class="recipe-card">
        <img src="${escapeHtml(image)}" alt="${escapeHtml(recipe.name)}" />
        <div class="recipe-card-body">
          <div class="recipe-top">
            <span class="match-badge">${recipe.matchPercentage || 0}% match</span>
            <button type="button" class="fav-button ${isFav ? 'active' : ''}" data-favorite-id="${id}" aria-label="Toggle favorite">${isFav ? '♥' : '♡'}</button>
          </div>
          <h3>${escapeHtml(recipe.name)}</h3>
          <p>${escapeHtml(desc)}</p>
          <div class="meta-row">${meta}</div>
          <div class="dietary-row">${dietary}</div>
          <div class="recipe-card-actions">
            <button class="secondary-btn" type="button" data-open-detail="${id}">View recipe</button>
            <span class="meta-pill">Missing: ${recipe.missingRequiredCount || 0}</span>
          </div>
        </div>
      </article>
    `;
  }).join('');

  container.querySelectorAll('[data-open-detail]').forEach(button => {
    button.addEventListener('click', () => onOpen(Number(button.dataset.openDetail)));
  });

  container.querySelectorAll('[data-favorite-id]').forEach(button => {
    button.addEventListener('click', () => onToggleFavorite(Number(button.dataset.favoriteId)));
  });
}

export function renderRecipeDetail(recipeDetail) {
  const container = document.getElementById('modal-content');
  if (!container) return;

  const recipe = recipeDetail.recipe || recipeDetail;
  const ingredients = recipeDetail.ingredients || [];
  const required = ingredients.filter(item => !item.optional);
  const optional = ingredients.filter(item => item.optional);

  const body = `
    <div class="detail-header">
      <img class="detail-cover" src="${escapeHtml(recipe.imageUrl || 'https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=900&q=80')}" alt="${escapeHtml(recipe.name)}" />
      <h2>${escapeHtml(recipe.name)}</h2>
      <p>${escapeHtml(recipe.description || '')}</p>
      <div class="detail-meta">
        <span>Prep: ${recipe.preparationTimeMinutes} min</span>
        <span>Cook: ${recipe.cookingTimeMinutes} min</span>
        <span>Total: ${recipe.totalTimeMinutes || (recipe.preparationTimeMinutes + recipe.cookingTimeMinutes)} min</span>
        <span>Serves: ${recipe.servings}</span>
        <span>Difficulty: ${recipe.difficulty}</span>
      </div>
      <div class="dietary-row">
        ${recipe.vegetarian ? '<span class="dietary-tag">Vegetarian</span>' : ''}
        ${recipe.vegan ? '<span class="dietary-tag">Vegan</span>' : ''}
        ${recipe.eggFree ? '<span class="dietary-tag">Egg-free</span>' : ''}
        ${recipe.dairyFree ? '<span class="dietary-tag">Dairy-free</span>' : ''}
      </div>
    </div>
    <div class="detail-layout">
      <div class="detail-ingredients">
        <h3>Ingredients</h3>
        <ul>
          ${required.map(item => `<li>${escapeHtml(item.quantity || '')} ${escapeHtml(item.unit || '')} ${escapeHtml(item.ingredientName)} <span class="ingredient-status">Required</span></li>`).join('') || '<li>No required ingredients listed.</li>'}
          ${optional.map(item => `<li>${escapeHtml(item.quantity || '')} ${escapeHtml(item.unit || '')} ${escapeHtml(item.ingredientName)} <span class="ingredient-status missing">Optional</span></li>`).join('')}
        </ul>
      </div>
      <div class="detail-steps">
        <h3>Instructions</h3>
        <ol>
          ${(recipe.instructions || []).map(step => `<li>${escapeHtml(step)}</li>`).join('') || '<li>No instructions available.</li>'}
        </ol>
      </div>
    </div>
  `;

  container.innerHTML = body;
}

export function getSortLabel(value) {
  if (value === 'shortest-time') return 'shortest time';
  if (value === 'alphabetical') return 'alphabetical';
  return 'best match';
}
