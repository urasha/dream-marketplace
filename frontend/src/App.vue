<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, RouterView } from 'vue-router'
import Header from './components/Header.vue'
import { useSessionStore } from './stores/session'
import { fetchWallet } from './api/payments'

const route = useRoute()

const userBalance = ref(0)

const session = useSessionStore()
const userRole = computed(() => session.role.value || 'user')
const isAuthenticated = computed(() => session.isAuthenticated.value)
const userName = computed(() => session.state.profile?.username || '')

const updateBalance = (value) => {
  userBalance.value = value
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

onMounted(async () => {
  try {
    await session.loadProfile()
    if (session.isAuthenticated.value) {
      await loadWallet()
    }
  } catch (e) {
    // ignore
  }
})
</script>

<template>
  <div class="min-h-screen bg-background text-foreground">
    <Header
      :current-page="route.name"
      :user-balance="userBalance"
      :user-role="userRole"
      :is-authenticated="isAuthenticated"
      :user-name="userName"
    />

    <main class="pt-16">
      <RouterView v-slot="{ Component }">
        <component
          :is="Component"
          :user-balance="userBalance"
          @update-balance="updateBalance"
        />
      </RouterView>
    </main>
  </div>
</template>
