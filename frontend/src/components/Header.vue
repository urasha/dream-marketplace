<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Menu, X, Search, User, Bell, LogOut } from 'lucide-vue-next'
import { useSessionStore } from '../stores/session'
import { useLotsStore } from '../stores/lots'
import { API_BASE } from '../api/httpClient'

const props = defineProps({
  currentPage: { type: String, required: false },
  userBalance: { type: Number, required: true },
  userRole: { type: String, default: 'user' },
  unreadNotifications: { type: Number, default: 3 },
  isAuthenticated: { type: Boolean, default: false },
  userName: { type: String, default: '' },
  userAvatar: { type: String, default: '' },
})

const router = useRouter()
const route = useRoute()
const session = useSessionStore()
const lotsStore = useLotsStore()
const mobileMenuOpen = ref(false)
const searchQuery = ref('')
const searchResults = ref([])
const searchLoading = ref(false)
const searchOpen = ref(false)
const searchBoxRef = ref(null)
const profileMenuOpen = ref(false)
const profileMenuRef = ref(null)
let searchTimer = null

const navItems = computed(() => {
  const base = [
    { id: 'home', label: 'Главная', action: () => router.push({ name: 'home' }) },
  ]
  if (props.isAuthenticated) {
    base.push(
      { id: 'dreams', label: 'Мои сны', action: () => router.push({ name: 'profile', query: { tab: 'dreams' } }) },
      { id: 'lots', label: 'Мои лоты', action: () => router.push({ name: 'profile', query: { tab: 'lots' } }) },
      { id: 'purchases', label: 'Мои покупки', action: () => router.push({ name: 'profile', query: { tab: 'purchases' } }) }
    )
  }
  if (props.userRole === 'admin') {
    base.push({ id: 'admin', label: 'Модерация', action: () => router.push({ name: 'admin' }) })
  }
  return base
})

const resolvedAvatar = computed(() => {
  const url = props.userAvatar || ''
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
    return url
  }
  if (url.startsWith('/')) {
    return `${API_BASE}${url}`
  }
  return url
})

const activeNavId = computed(() => {
  if (route.name === 'profile') {
    return route.query.tab || 'dreams'
  }
  if (route.name === 'admin') return 'admin'
  if (route.name === 'home') return 'home'
  return null
})

const navButtonClass = (item) => {
  const isActive = activeNavId.value === item.id
  const base = 'px-2 py-1 rounded-lg text-sm font-medium whitespace-nowrap transition-colors'
  if (isActive && item.id === 'admin') {
    return `${base} bg-red-600 text-white`
  }
  if (isActive) {
    return `${base} bg-violet-600 text-white`
  }
  return `${base} text-gray-700 hover:bg-gray-100`
}

const go = (page) => {
  const map = {
    home: () => router.push({ name: 'home' }),
    admin: () => router.push({ name: 'admin' }),
    notifications: () => router.push({ name: 'notifications' }),
    wallet: () => router.push({ name: 'wallet' }),
    profile: () => router.push({ name: 'profile' }),
    dreams: () => router.push({ name: 'profile', query: { tab: 'dreams' } }),
    lots: () => router.push({ name: 'profile', query: { tab: 'lots' } }),
    purchases: () => router.push({ name: 'profile', query: { tab: 'purchases' } }),
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
  if (searchBoxRef.value && !searchBoxRef.value.contains(event.target)) {
    searchOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})

const normalize = (value) => (value || '').toString().toLowerCase()

const runSearch = async (term) => {
  const query = normalize(term).trim()
  if (!query) {
    searchResults.value = []
    searchOpen.value = false
    return
  }

  searchLoading.value = true
  if (!lotsStore.state.catalog.length) {
    await lotsStore.loadCatalog().catch(() => {})
  }

  const results = lotsStore.state.catalog.filter((lot) => {
    const title = normalize(lot.title)
    const author = normalize(lot.authorName)
    const description = normalize(lot.description)
    const tags = (lot.tags || []).map((t) => normalize(t)).join(' ')
    return (
      title.includes(query) ||
      author.includes(query) ||
      description.includes(query) ||
      tags.includes(query)
    )
  })

  searchResults.value = results.slice(0, 6)
  searchOpen.value = true
  searchLoading.value = false
}

watch(searchQuery, (value) => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => runSearch(value), 250)
})

const handleSearchSelect = (lotId) => {
  searchOpen.value = false
  searchQuery.value = ''
  if (lotId) {
    router.push({ name: 'lot-detail', params: { id: lotId } })
  }
}

const handleSearchSubmit = () => {
  if (!searchQuery.value.trim()) return
  searchOpen.value = true
}
</script>

<template>

  <header class="fixed top-0 left-0 right-0 h-16 bg-white shadow-sm border-b border-gray-200 z-50">
    <div class="max-w-[1160px] mx-auto px-2 h-full flex items-center justify-between">
      <button @click="go('home')" class="hover:opacity-80 transition-opacity min-w-[120px] p-0">
        <div class="tracking-tight bg-gradient-to-r from-violet-600 to-indigo-600 bg-clip-text text-transparent text-base">
          Dream Marketplace
        </div>
      </button>

      <nav class="hidden md:flex items-center gap-0.5 flex-nowrap overflow-x-auto min-w-0">
        <div class="relative min-w-0" ref="searchBoxRef">
          <Search class="absolute left-2 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Поиск..."
            :class="'pl-10 pr-2 py-1 bg-white border border-gray-200 rounded-lg text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent text-xs ' + (userRole === 'admin' ? 'w-28' : 'w-36')"
            @focus="searchOpen = true"
            @keydown.enter.prevent="handleSearchSubmit"
          />

          <div
            v-if="searchOpen && (searchLoading || searchResults.length)"
            class="absolute left-0 right-0 mt-2 bg-white border border-gray-200 rounded-lg shadow-lg z-50 overflow-hidden"
          >
            <div v-if="searchLoading" class="px-4 py-3 text-sm text-gray-600">Ищем...</div>
            <button
              v-for="lot in searchResults"
              :key="lot.id"
              class="w-full text-left px-4 py-3 hover:bg-violet-50 transition-colors"
              @click="handleSearchSelect(lot.id)"
            >
              <div class="text-sm font-medium text-gray-900">{{ lot.title }}</div>
              <div class="text-xs text-gray-500">{{ lot.authorName || '—' }}</div>
            </button>
          </div>
        </div>

        <button
          v-for="item in navItems"
          :key="item.id"
          @click="item.action()"
          :class="navButtonClass(item) + ' text-xs px-2 py-1'"
          style="min-width: 0;"
        >
          {{ item.label }}
        </button>

        <div class="flex items-center gap-2 pl-2 border-l border-gray-200 ml-1 min-w-0">
          <button
            @click="go('wallet')"
            class="px-2 py-1 bg-green-600 text-white rounded-lg border border-green-600 hover:bg-green-700 hover:border-green-700 transition-colors text-xs min-w-0"
            style="min-width: 0;"
          >
            {{ userBalance }} ₽
          </button>

          <button
            @click="goNotifications"
            class="relative p-1 hover:bg-gray-100 rounded-lg transition-colors min-w-0"
            style="min-width: 0;"
          >
            <Bell class="w-4 h-4 text-gray-700" />
            <span v-if="unreadNotifications > 0" class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" />
          </button>

          <div class="relative min-w-0" ref="profileMenuRef">
            <button
              @click.stop="toggleProfileMenu"
              class="p-1 rounded-lg border border-gray-200 hover:border-violet-400 hover:bg-violet-50 transition-colors flex items-center gap-1 min-w-0"
              style="min-width: 0;"
            >
              <img
                v-if="resolvedAvatar"
                :src="resolvedAvatar"
                alt="Аватар"
                class="w-5 h-5 rounded-full object-cover"
              />
              <User v-else class="w-4 h-4 text-gray-700" />
              <span v-if="isAuthenticated" class="text-xs text-gray-800 font-medium max-w-[80px] truncate">{{ userName || 'Профиль' }}</span>
            </button>

            <div
              v-if="profileMenuOpen"
              class="absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg py-2 z-50"
            >
              <div v-if="isAuthenticated" class="px-4 py-2 border-b border-gray-100 text-xs text-gray-700">
                {{ userName || 'Профиль' }}
              </div>

              <button
                v-if="isAuthenticated"
                @click="goProfile"
                class="w-full flex items-center gap-2 px-4 py-2 text-left text-gray-800 hover:bg-violet-50 text-xs"
              >
                <User class="w-4 h-4" />
                Профиль
              </button>
              <button
                v-if="isAuthenticated"
                @click="handleLogout"
                class="w-full flex items-center gap-2 px-4 py-2 text-left text-red-600 hover:text-red-700 hover:bg-violet-50 text-xs"
              >
                <LogOut class="w-4 h-4" />
                Выйти
              </button>

              <template v-else>
                <button
                  @click="() => { profileMenuOpen = false; startAuth() }"
                  class="w-full flex items-center gap-2 px-4 py-2 text-left text-violet-700 hover:bg-violet-50 text-xs"
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
        <div class="relative mb-4" ref="searchBoxRef">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Поиск..."
            class="w-full pl-10 pr-4 py-2 bg-white border border-gray-200 rounded-lg text-gray-900 placeholder-gray-400"
            @focus="searchOpen = true"
            @keydown.enter.prevent="handleSearchSubmit"
          />

          <div
            v-if="searchOpen && (searchLoading || searchResults.length)"
            class="absolute left-0 right-0 mt-2 bg-white border border-gray-200 rounded-lg shadow-lg z-50 overflow-hidden"
          >
            <div v-if="searchLoading" class="px-4 py-3 text-sm text-gray-600">Ищем...</div>
            <button
              v-for="lot in searchResults"
              :key="lot.id"
              class="w-full text-left px-4 py-3 hover:bg-violet-50 transition-colors"
              @click="() => { handleSearchSelect(lot.id); mobileMenuOpen = false }"
            >
              <div class="text-sm font-medium text-gray-900">{{ lot.title }}</div>
              <div class="text-xs text-gray-500">{{ lot.authorName || '—' }}</div>
            </button>
          </div>
        </div>

        <div class="flex flex-col gap-3">
          <button
            v-for="item in navItems"
            :key="item.id"
            @click="() => { item.action(); mobileMenuOpen = false }"
            class="text-left py-2 transition-colors whitespace-nowrap"
            :class="navButtonClass(item)"
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
                <img
                  v-if="resolvedAvatar"
                  :src="resolvedAvatar"
                  alt="Аватар"
                  class="w-6 h-6 rounded-full object-cover"
                />
                <User v-else class="w-5 h-5" />
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
            <button
              @click="() => { go('wallet'); mobileMenuOpen = false }"
              class="px-3 py-1 bg-green-600 text-white rounded-lg inline-block border border-green-600 hover:bg-green-700 hover:border-green-700 transition-colors"
            >
              Баланс: {{ userBalance }} ₽
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>
