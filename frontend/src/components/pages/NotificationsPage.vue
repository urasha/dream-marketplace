<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from 'lucide-vue-next'
import {
  fetchNotifications,
  fetchUnreadCount,
  markAllNotificationsRead,
  markNotificationRead,
} from '../../api/notifications'

const router = useRouter()

const notifications = ref([])
const loading = ref(true)
const error = ref(null)
const pollingId = ref(null)
const autoReadTimers = new Map()
const knownIds = new Set()

const emit = defineEmits(['notifications-updated'])

const formatDate = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString('ru-RU')
}

const isClickable = (notification) => Boolean(notification?.targetLotId)

const refreshUnreadCount = async () => {
  try {
    const data = await fetchUnreadCount()
    emit('notifications-updated', data?.count ?? 0)
  } catch (e) {
    // ignore
  }
}

const scheduleAutoRead = (notification) => {
  if (!notification || notification.isRead || isClickable(notification)) return
  if (autoReadTimers.has(notification.id)) return
  const timer = setTimeout(async () => {
    try {
      await markNotificationRead(notification.id)
      notifications.value = notifications.value.map((item) =>
        item.id === notification.id ? { ...item, isRead: true } : item
      )
      await refreshUnreadCount()
    } finally {
      autoReadTimers.delete(notification.id)
    }
  }, 1500)
  autoReadTimers.set(notification.id, timer)
}

const handleNotificationClick = async (notification) => {
  if (!isClickable(notification)) return
  try {
    await markNotificationRead(notification.id)
    notifications.value = notifications.value.map((item) =>
      item.id === notification.id ? { ...item, isRead: true } : item
    )
    await refreshUnreadCount()
  } catch (e) {
    // ignore read errors
  }

  const query = {}
  if (notification.targetCommentId) {
    query.commentId = notification.targetCommentId
  }
  router.push({ name: 'lot-detail', params: { id: notification.targetLotId }, query })
}

const loadNotifications = async () => {
  loading.value = true
  error.value = null
  try {
    const list = await fetchNotifications()
    const normalized = Array.isArray(list) ? list : []
    notifications.value = normalized
    normalized.forEach((item) => {
      if (!knownIds.has(item.id)) {
        knownIds.add(item.id)
        scheduleAutoRead(item)
      } else if (!item.isRead) {
        scheduleAutoRead(item)
      }
    })
    await refreshUnreadCount()
  } catch (e) {
    error.value = 'Не удалось загрузить уведомления'
  } finally {
    loading.value = false
  }
}

const readAll = async () => {
  try {
    await markAllNotificationsRead()
    notifications.value = notifications.value.map((item) => ({ ...item, isRead: true }))
    await refreshUnreadCount()
  } catch (e) {
    // ignore
  }
}

onMounted(() => {
  loadNotifications()
  pollingId.value = setInterval(loadNotifications, 10000)
})

onBeforeUnmount(() => {
  if (pollingId.value) {
    clearInterval(pollingId.value)
  }
  autoReadTimers.forEach((timer) => clearTimeout(timer))
  autoReadTimers.clear()
})
</script>

<template>
  <div class="max-w-[800px] mx-auto px-6 py-12">
    <div class="flex items-center gap-3 mb-8">
      <Bell class="w-8 h-8 text-violet-600" />
      <h1>Уведомления</h1>
    </div>

    <div v-if="loading" class="text-gray-600">Загрузка...</div>
    <div v-else-if="error" class="text-red-600">{{ error }}</div>
    <div v-else-if="!notifications.length" class="text-gray-600">Уведомлений пока нет</div>

    <div v-else class="space-y-4">
      <div class="flex items-center justify-end">
        <button
          class="px-4 py-2 text-sm bg-violet-600 text-white rounded-lg hover:bg-violet-700 transition-colors"
          @click="readAll"
        >
          Прочитать всё
        </button>
      </div>
      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="p-6 rounded-xl border transition-all"
        :class="notification.isRead
          ? 'bg-white border-gray-200'
          : 'bg-violet-100 border-violet-300 shadow-md'
        "
        role="button"
        :tabindex="isClickable(notification) ? 0 : -1"
        :style="isClickable(notification) ? 'cursor: pointer;' : ''"
        @click="isClickable(notification) ? handleNotificationClick(notification) : null"
      >
        <div class="flex items-start justify-between mb-3">
          <div :class="notification.isRead ? 'text-gray-800' : 'text-gray-900'" class="flex-1">
            {{ notification.message }}
          </div>
          <span v-if="!notification.isRead" class="ml-3 w-2 h-2 bg-violet-600 rounded-full flex-shrink-0 mt-2" />
        </div>

        <div class="text-gray-600">{{ formatDate(notification.createdAt) }}</div>
      </div>
    </div>
  </div>
</template>
