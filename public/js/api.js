export async function requestJson(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      'Accept': 'application/json',
      ...(options.headers || {})
    },
    ...options
  });

  let payload = null;
  const rawText = await response.text();
  if (rawText) {
    try {
      payload = JSON.parse(rawText);
    } catch (error) {
      throw new Error('Server returned an invalid JSON response.');
    }
  }

  if (!response.ok) {
    const message = payload && payload.error && payload.error.message
      ? payload.error.message
      : 'Request failed.';
    throw new Error(message);
  }

  return payload;
}

export function buildQueryString(params) {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return;
    }
    searchParams.append(key, String(value));
  });
  const query = searchParams.toString();
  return query ? `?${query}` : '';
}
