import { reactive } from 'vue'

let toastId = 1

export const feedbackState = reactive({
  toasts: [],
  confirm: null,
})

export function showToast({ message, type = 'error', duration = 4500 }) {
  if (!message) return
  const id = toastId++
  const toast = { id, message, type }
  feedbackState.toasts.push(toast)
  if (duration && duration > 0) {
    setTimeout(() => removeToast(id), duration)
  }
}

export function showError(message) {
  showToast({ message, type: 'error' })
}

export function removeToast(id) {
  const idx = feedbackState.toasts.findIndex((t) => t.id === id)
  if (idx >= 0) {
    feedbackState.toasts.splice(idx, 1)
  }
}

export function confirmAction({
  title = 'Подтвердите действие',
  message = 'Вы уверены?',
  confirmText = 'Подтвердить',
  cancelText = 'Отмена',
  tone = 'danger',
} = {}) {
  return new Promise((resolve) => {
    feedbackState.confirm = {
      title,
      message,
      confirmText,
      cancelText,
      tone,
      resolve,
    }
  })
}

export function resolveConfirm(value) {
  if (feedbackState.confirm?.resolve) {
    feedbackState.confirm.resolve(Boolean(value))
  }
  feedbackState.confirm = null
}

const englishToRussian = {
  'Access denied': 'Доступ запрещен',
  'Authentication required': 'Требуется авторизация',
  Unauthorized: 'Требуется авторизация',
  'Request failed': 'Не удалось выполнить запрос',
  'Download failed': 'Не удалось скачать файл',
  'User not found': 'Пользователь не найден',
  'Lot not found': 'Лот не найден',
  'Dream not found': 'Сон не найден',
}

function translateMessage(message) {
  if (!message) return ''
  return englishToRussian[message] || message
}

export function normalizeErrorMessage(error, fallback = 'Произошла ошибка') {
  const status = error?.status || error?.response?.status
  const data = error?.data
  const rawMessage = data?.message || data?.error || error?.message
  const translated = translateMessage(rawMessage)

  if (translated && translated !== 'Request failed' && translated !== 'Unauthorized') {
    return translated
  }

  const statusMap = {
    400: 'Некорректный запрос',
    401: 'Требуется авторизация',
    403: 'Доступ запрещен',
    404: 'Ресурс не найден',
    409: 'Конфликт данных',
    422: 'Ошибка валидации',
    429: 'Слишком много запросов',
    500: 'Ошибка сервера',
    502: 'Ошибка шлюза',
    503: 'Сервис временно недоступен',
  }

  if (status && statusMap[status]) {
    return statusMap[status]
  }

  return fallback
}
