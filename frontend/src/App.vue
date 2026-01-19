<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRoute, RouterView } from 'vue-router'
import Header from './components/Header.vue'
import { useSessionStore } from './stores/session'
import { fetchWallet } from './api/payments'
import { fetchUnreadCount } from './api/notifications'

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

onMounted(async () => {
  try {
    await session.loadProfile()
    if (session.isAuthenticated.value) {
      await loadWallet()
      await startNotificationPolling()
    } else {
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
  </div>
</template>
