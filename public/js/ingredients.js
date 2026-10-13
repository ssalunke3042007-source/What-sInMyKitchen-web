import { requestJson } from './api.js';

export async function loadIngredientsAndCategories() {
  const [ingredientsResponse, categoriesResponse] = await Promise.all([
    requestJson('/api/ingredients?limit=200'),
    requestJson('/api/categories')
  ]);

  const ingredients = Array.isArray(ingredientsResponse && ingredientsResponse.items) ? ingredientsResponse.items : [];
  const categories = Array.isArray(categoriesResponse && categoriesResponse.items) ? categoriesResponse.items : [];
  return { ingredients, categories };
}

export function renderCategoryOptions(categories) {
  const select = document.getElementById('category-filter');
  if (!select) return;
  const options = ['<option value="">All categories</option>'];
  categories.forEach(category => {
    options.push(`<option value="${category.name}">${category.name}</option>`);
  });
  select.innerHTML = options.join('');
}

export function renderIngredientList(ingredients, selectedIds) {
  const list = document.getElementById('ingredient-list');
  if (!list) return;

  if (!ingredients.length) {
    list.innerHTML = '<div class="empty-state"><h3>No ingredients found</h3><p>Try a different search or category filter.</p></div>';
    return;
  }

  list.innerHTML = ingredients.map(item => {
    const selected = selectedIds.has(Number(item.id));
    return `
      <button type="button" class="ingredient-btn ${selected ? 'selected' : ''}" data-ingredient-id="${item.id}">
        ${escapeHtml(item.name)}
      </button>
    `;
  }).join('');
}

export function renderSelectedChips(ingredients, selectedIds) {
  const container = document.getElementById('selected-chips');
  const counter = document.getElementById('selection-counter');
  if (!container || !counter) return;

  const selected = ingredients.filter(item => selectedIds.has(Number(item.id)));
  container.innerHTML = selected.length
    ? selected.map(item => `
        <span class="selected-chip">
          <span>${escapeHtml(item.name)}</span>
          <button type="button" data-remove-id="${item.id}" aria-label="Remove ${escapeHtml(item.name)}">×</button>
        </span>
      `).join('')
    : '<span class="empty-state" style="width:100%;padding:12px 10px;">No ingredients selected yet.</span>';

  counter.textContent = `${selected.length} selected`;
}

export function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
