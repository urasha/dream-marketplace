<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, RouterView } from 'vue-router'
import Header from './components/Header.vue'
import { useSessionStore } from './stores/session'

const route = useRoute()

const userBalance = ref(0)

const session = useSessionStore()
const userRole = computed(() => session.role.value || 'user')

const updateBalance = (value) => {
  userBalance.value = value
}

onMounted(() => {
  session.loadProfile().catch(() => {})
})
</script>

<template>
  <div class="min-h-screen bg-background text-foreground">
    <Header
      :current-page="route.name"
      :user-balance="userBalance"
      :user-role="userRole"
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
