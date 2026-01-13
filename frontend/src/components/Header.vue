<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Menu, X, Search, User, Bell, LogOut } from 'lucide-vue-next'
import { useSessionStore } from '../stores/session'

const props = defineProps({
  currentPage: { type: String, required: false },
  userBalance: { type: Number, required: true },
  userRole: { type: String, default: 'user' },
  unreadNotifications: { type: Number, default: 3 },
  isAuthenticated: { type: Boolean, default: false },
  userName: { type: String, default: '' },
})

const router = useRouter()
const session = useSessionStore()
const mobileMenuOpen = ref(false)
const searchQuery = ref('')
const profileMenuOpen = ref(false)
const profileMenuRef = ref(null)

const navItems = computed(() => {
  const base = [{ id: 'home', label: 'Главная' }]
  if (props.userRole === 'admin') {
    base.push({ id: 'admin', label: 'Админ' })
  }
  return base
})

const go = (page) => {
  const map = {
    home: () => router.push({ name: 'home' }),
    admin: () => router.push({ name: 'admin' }),
    notifications: () => router.push({ name: 'notifications' }),
    profile: () => router.push({ name: 'profile' }),
  }
  map[page]?.()
}

const goNotifications = () => go('notifications')
const goProfile = () => {
  profileMenuOpen.value = false
  go('profile')
}
const startAuth = () => {
  // Redirect to backend OAuth entrypoint (same for login/registration)
  const base = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  window.location.href = `${base}/oauth/yandex/login`
}

const handleLogout = async () => {
  await session.logout()
  router.push({ name: 'home' })
  profileMenuOpen.value = false
}

const toggleProfileMenu = () => {
  profileMenuOpen.value = !profileMenuOpen.value
}

const handleClickOutside = (event) => {
  if (!profileMenuRef.value) return
  if (!profileMenuRef.value.contains(event.target)) {
    profileMenuOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <header class="fixed top-0 left-0 right-0 h-16 bg-white shadow-sm border-b border-gray-200 z-50">
    <div class="max-w-[1160px] mx-auto px-6 h-full flex items-center justify-between">
      <button @click="go('home')" class="hover:opacity-80 transition-opacity">
        <div class="tracking-tight bg-gradient-to-r from-violet-600 to-indigo-600 bg-clip-text text-transparent">
          Dream Marketplace
        </div>
      </button>

      <nav class="hidden md:flex items-center gap-6">
        <div class="relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Поиск..."
            class="pl-10 pr-4 py-2 bg-white border border-gray-200 rounded-lg text-gray-900 placeholder-gray-400 w-64 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent"
          />
        </div>

        <button
          v-for="item in navItems"
          :key="item.id"
          @click="go(item.id)"
          class="hover:text-violet-600 transition-colors"
          :class="currentPage === item.id ? 'text-violet-600' : 'text-gray-700'"
        >
          {{ item.label }}
        </button>

        <div class="flex items-center gap-3 pl-4 border-l border-gray-200">
          <span class="px-3 py-1 bg-violet-50 text-violet-700 rounded-lg">{{ userBalance }} ₽</span>

          <button
            @click="goNotifications"
            class="relative p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <Bell class="w-5 h-5 text-gray-700" />
            <span v-if="unreadNotifications > 0" class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" />
          </button>

          <div class="relative" ref="profileMenuRef">
            <button
              @click.stop="toggleProfileMenu"
              class="p-2 rounded-lg border border-gray-200 hover:border-violet-400 hover:bg-violet-50 transition-colors flex items-center gap-2"
            >
              <User class="w-5 h-5 text-gray-700" />
              <span v-if="isAuthenticated" class="text-sm text-gray-800 font-medium">{{ userName || 'Профиль' }}</span>
            </button>

            <div
              v-if="profileMenuOpen"
              class="absolute right-0 mt-2 w-56 bg-white border border-gray-200 rounded-lg shadow-lg py-2 z-50"
            >
              <div v-if="isAuthenticated" class="px-4 py-2 border-b border-gray-100 text-sm text-gray-700">
                {{ userName || 'Профиль' }}
              </div>

              <button
                v-if="isAuthenticated"
                @click="goProfile"
                class="w-full flex items-center gap-2 px-4 py-2 text-left text-gray-800 hover:bg-violet-50"
              >
                <User class="w-4 h-4" />
                Перейти в профиль
              </button>
              <button
                v-if="isAuthenticated"
                @click="handleLogout"
                class="w-full flex items-center gap-2 px-4 py-2 text-left text-red-600 hover:text-red-700 hover:bg-violet-50"
              >
                <LogOut class="w-4 h-4" />
                Выйти
              </button>

              <template v-else>
                <button
                  @click="() => { profileMenuOpen = false; startAuth() }"
                  class="w-full flex items-center gap-2 px-4 py-2 text-left text-violet-700 hover:bg-violet-50"
                >
                  <User class="w-4 h-4" />
                  Войти с Yandex
                </button>
              </template>
            </div>
          </div>
        </div>
      </nav>

      <button @click="mobileMenuOpen = !mobileMenuOpen" class="md:hidden hover:opacity-80">
        <X v-if="mobileMenuOpen" class="w-6 h-6" />
        <Menu v-else class="w-6 h-6" />
      </button>
    </div>

    <div v-if="mobileMenuOpen" class="md:hidden absolute top-16 left-0 right-0 bg-white border-b border-gray-200 shadow-lg">
      <div class="px-6 py-4">
        <div class="relative mb-4">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Поиск..."
            class="w-full pl-10 pr-4 py-2 bg-white border border-gray-200 rounded-lg text-gray-900 placeholder-gray-400"
          />
        </div>

        <div class="flex flex-col gap-3">
          <button
            v-for="item in navItems"
            :key="item.id"
            @click="() => { go(item.id); mobileMenuOpen = false }"
            class="text-left py-2 hover:text-violet-600 transition-colors"
            :class="currentPage === item.id ? 'text-violet-600' : 'text-gray-700'"
          >
            {{ item.label }}
          </button>

          <div class="flex items-center gap-3 pt-3 mt-3 border-t border-gray-200">
            <button
              @click="() => { goNotifications(); mobileMenuOpen = false }"
              class="flex items-center gap-2 text-gray-700"
            >
              <Bell class="w-5 h-5" />
              Уведомления
              <span v-if="unreadNotifications > 0" class="px-2 py-0.5 bg-red-500 text-white rounded-full text-sm">{{ unreadNotifications }}</span>
            </button>
          </div>

          <div class="flex flex-col gap-2 text-gray-700">
            <template v-if="isAuthenticated">
              <button
                @click="() => { goProfile(); mobileMenuOpen = false }"
                class="flex items-center gap-2"
              >
                <User class="w-5 h-5" />
                <span class="font-medium">{{ userName || 'Профиль' }}</span>
              </button>
              <button
                @click="() => { handleLogout(); mobileMenuOpen = false }"
                class="flex items-center gap-2 text-red-600 hover:text-red-700"
              >
                <LogOut class="w-5 h-5" />
                Выйти
              </button>
            </template>

            <template v-else>
              <button
                @click="() => { startAuth(); mobileMenuOpen = false }"
                class="flex items-center gap-2 text-violet-700 font-medium hover:text-violet-800"
              >
                <User class="w-5 h-5" />
                Войти с Yandex
              </button>
            </template>
          </div>

          <div class="pt-3 mt-3 border-t border-gray-200">
            <span class="px-3 py-1 bg-violet-50 text-violet-700 rounded-lg inline-block">Баланс: {{ userBalance }} ₽</span>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>
