import { showError, normalizeErrorMessage } from '../ui/feedback'

export const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080'

function getToken() {
  try {
    const cookieToken = document.cookie
      .split(';')
      .map((c) => c.trim())
      .find((c) => c.startsWith('access_token='))
    if (cookieToken) {
      return decodeURIComponent(cookieToken.split('=')[1]) || null
    }
    const local = localStorage.getItem('access_token')
    return local || null
  } catch (e) {
    return null
  }
}

export function getAuthToken() {
  return getToken()
}

async function parseJsonSafe(response) {
  try {
    return await response.json()
  } catch (e) {
    return null
  }
}

async function request(path, { method = 'GET', body, headers = {} } = {}) {
  const token = getToken()
  const init = {
    method,
    headers: {
      Accept: 'application/json',
      ...headers,
    },
    credentials: 'include',
  }

  if (body !== undefined) {
    init.headers['Content-Type'] = 'application/json'
    init.body = JSON.stringify(body)
  }

  if (token) {
    init.headers.Authorization = `Bearer ${token}`
  }

  let response
  try {
    response = await fetch(`${API_BASE}${path}`, init)
  } catch (err) {
    const error = new Error('Network error')
    error.userMessage = 'Ошибка сети. Проверьте подключение.'
    showError(error.userMessage)
    throw error
  }
  const data = await parseJsonSafe(response)

  if (response.status === 401) {
    window.location.href = `${API_BASE}/oauth/yandex/login`
    const error = new Error('Unauthorized')
    error.status = 401
    error.data = data
    error.userMessage = normalizeErrorMessage(error)
    throw error
  }

  if (!response.ok) {
    const error = new Error('Request failed')
    error.status = response.status
    error.data = data
    error.userMessage = normalizeErrorMessage(error)
    showError(error.userMessage)
    throw error
  }

  return data
}

export const httpClient = {
  get: (path, options) => request(path, { ...options, method: 'GET' }),
  post: (path, body, options) => request(path, { ...options, method: 'POST', body }),
  patch: (path, body, options) => request(path, { ...options, method: 'PATCH', body }),
  put: (path, body, options) => request(path, { ...options, method: 'PUT', body }),
  delete: (path, options) => request(path, { ...options, method: 'DELETE' }),
}
