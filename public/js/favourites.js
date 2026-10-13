const FAVORITES_KEY = 'whatsinmykitchen:favorites';

export function getFavorites() {
  try {
    const raw = localStorage.getItem(FAVORITES_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed.map(Number).filter(Boolean) : [];
  } catch (error) {
    return [];
  }
}

export function saveFavorites(ids) {
  localStorage.setItem(FAVORITES_KEY, JSON.stringify(ids));
}

export function isFavorite(id) {
  return getFavorites().includes(Number(id));
}

export function toggleFavorite(id) {
  const favorites = getFavorites();
  const safeId = Number(id);
  const exists = favorites.includes(safeId);
  const next = exists ? favorites.filter(item => item !== safeId) : [...favorites, safeId];
  saveFavorites(next);
  return !exists;
}
