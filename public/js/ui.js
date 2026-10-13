export function showToast(message, type = 'info') {
  const root = document.getElementById('toast-root');
  if (!root) return;
  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.textContent = message;
  if (type === 'error') {
    toast.style.background = '#a43d27';
  }
  if (type === 'success') {
    toast.style.background = '#1b8f5d';
  }
  root.appendChild(toast);
  setTimeout(() => toast.remove(), 2500);
}

export function setLoading(element, isLoading, label = 'Loading...') {
  if (!element) return;
  if (isLoading) {
    element.disabled = true;
    element.dataset.originalText = element.textContent || '';
    element.textContent = label;
  } else {
    element.disabled = false;
    if (element.dataset.originalText) {
      element.textContent = element.dataset.originalText;
    }
  }
}

export function renderEmptyState(container, title, description) {
  if (!container) return;
  container.innerHTML = `
    <div class="empty-state">
      <h3>${escapeHtml(title)}</h3>
      <p>${escapeHtml(description)}</p>
    </div>
  `;
}

export function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

export function showModal() {
  const modal = document.getElementById('recipe-modal');
  if (modal) {
    modal.classList.remove('hidden');
    modal.setAttribute('aria-hidden', 'false');
  }
}

export function hideModal() {
  const modal = document.getElementById('recipe-modal');
  if (modal) {
    modal.classList.add('hidden');
    modal.setAttribute('aria-hidden', 'true');
  }
}
