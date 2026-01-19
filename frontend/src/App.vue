<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRoute, RouterView } from 'vue-router'
import Header from './components/Header.vue'
import { useSessionStore } from './stores/session'
import { fetchWallet } from './api/payments'
import { fetchUnreadCount } from './api/notifications'
import { feedbackState, removeToast, resolveConfirm } from './ui/feedback'

const route = useRoute()

const userBalance = ref(0)
const unreadNotifications = ref(0)
const notificationPollId = ref(null)

const session = useSessionStore()
const userRole = computed(() => session.role.value || 'user')
const isAuthenticated = computed(() => session.isAuthenticated.value)
const userName = computed(() => session.state.profile?.username || '')

const updateBalance = (value) => {
  userBalance.value = value
}

const updateUnreadNotifications = (value) => {
  unreadNotifications.value = Number(value) || 0
}

const playNotificationSound = () => {
  try {
    const AudioContextClass = window.AudioContext || window.webkitAudioContext
    if (!AudioContextClass) return
    const ctx = new AudioContextClass()
    const oscillator = ctx.createOscillator()
    const gain = ctx.createGain()
    oscillator.type = 'sine'
    oscillator.frequency.value = 880
    gain.gain.value = 0.05
    oscillator.connect(gain)
    gain.connect(ctx.destination)
    oscillator.start()
    setTimeout(() => {
      oscillator.stop()
      ctx.close()
    }, 180)
  } catch (e) {
    // ignore audio errors
  }
}

const loadWallet = async () => {
  try {
    const wallet = await fetchWallet()
    if (wallet?.balance !== undefined) {
      userBalance.value = Number(wallet.balance) || 0
    }
  } catch (e) {
    // ignore wallet load errors
  }
}

const loadUnreadNotifications = async () => {
  try {
    const data = await fetchUnreadCount()
    if (data?.count !== undefined) {
      const nextCount = Number(data.count) || 0
      if (nextCount > unreadNotifications.value) {
        playNotificationSound()
      }
      unreadNotifications.value = nextCount
    }
  } catch (e) {
    // ignore notification load errors
  }
}

const startNotificationPolling = async () => {
  await loadUnreadNotifications()
  if (notificationPollId.value) {
    clearInterval(notificationPollId.value)
  }
  notificationPollId.value = setInterval(loadUnreadNotifications, 10000)
}

const stopNotificationPolling = () => {
  if (notificationPollId.value) {
    clearInterval(notificationPollId.value)
    notificationPollId.value = null
  }
}

const closeConfirm = () => resolveConfirm(false)
const confirmOk = () => resolveConfirm(true)

onMounted(async () => {
  try {
    await session.loadProfile()
    if (session.isAuthenticated.value) {
      await loadWallet()
      await startNotificationPolling()
    } else {
      userBalance.value = 0
      unreadNotifications.value = 0
      stopNotificationPolling()
    }
  } catch (e) {
    // ignore
  }
})

watch(
  () => session.isAuthenticated.value,
  async (value) => {
    if (value) {
      await loadWallet()
      await startNotificationPolling()
    } else {
      userBalance.value = 0
      unreadNotifications.value = 0
      stopNotificationPolling()
    }
  }
)

onBeforeUnmount(() => {
  if (notificationPollId.value) {
    clearInterval(notificationPollId.value)
  }
})
</script>

<template>
  <div class="min-h-screen bg-background text-foreground">
    <Header
      :current-page="route.name"
      :user-balance="userBalance"
      :user-role="userRole"
      :unread-notifications="unreadNotifications"
      :is-authenticated="isAuthenticated"
      :user-name="userName"
    />

    <main class="pt-16">
      <RouterView v-slot="{ Component }">
        <component
          :is="Component"
          :user-balance="userBalance"
          @update-balance="updateBalance"
          @notifications-updated="updateUnreadNotifications"
        />
      </RouterView>
    </main>

    <div class="toast-stack">
      <div
        v-for="toast in feedbackState.toasts"
        :key="toast.id"
        class="toast"
        :class="toast.type === 'error' ? 'toast-error' : 'toast-info'"
      >
        <span>{{ toast.message }}</span>
        <button class="toast-close" @click="removeToast(toast.id)">×</button>
      </div>
    </div>

    <div v-if="feedbackState.confirm" class="confirm-overlay" @click.self="closeConfirm">
      <div class="confirm-card">
        <div class="confirm-title">{{ feedbackState.confirm.title }}</div>
        <div class="confirm-message">{{ feedbackState.confirm.message }}</div>
        <div class="confirm-actions">
          <button class="confirm-cancel" @click="closeConfirm">{{ feedbackState.confirm.cancelText }}</button>
          <button
            class="confirm-accept"
            :class="feedbackState.confirm.tone === 'danger' ? 'confirm-danger' : 'confirm-primary'"
            @click="confirmOk"
          >
            {{ feedbackState.confirm.confirmText }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
.toast-stack {
  position: fixed;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 1100;
  width: min(720px, 92vw);
}

.toast {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  padding: 18px 20px;
  border-radius: 16px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.2);
  background: #fff;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.toast-error {
  border: 2px solid #fecaca;
  background: #fef2f2;
  color: #b91c1c;
}

.toast-info {
  border: 2px solid #bfdbfe;
  background: #eff6ff;
  color: #1d4ed8;
}

.toast-close {
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 22px;
  color: inherit;
  line-height: 1;
}

.confirm-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(4px);
  z-index: 1000;
}

.confirm-card {
  width: min(420px, 92vw);
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.25);
}

.confirm-title {
  font-weight: 600;
  font-size: 18px;
  margin-bottom: 8px;
}

.confirm-message {
  color: #4b5563;
  margin-bottom: 20px;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.confirm-cancel {
  padding: 8px 14px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #374151;
}

.confirm-accept {
  padding: 8px 14px;
  border-radius: 10px;
  border: none;
  color: #fff;
}

.confirm-danger {
  background: #ef4444;
}

.confirm-primary {
  background: #7c3aed;
}
</style>
