<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Menu, X, Search, User, Bell } from 'lucide-vue-next'

const props = defineProps({
  currentPage: { type: String, required: false },
  userBalance: { type: Number, required: true },
  userRole: { type: String, default: 'user' },
  unreadNotifications: { type: Number, default: 3 },
})

const router = useRouter()
const mobileMenuOpen = ref(false)
const searchQuery = ref('')

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
const goProfile = () => go('profile')
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

          <button @click="goProfile" class="p-2 hover:bg-gray-100 rounded-lg transition-colors">
            <User class="w-5 h-5 text-gray-700" />
          </button>
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

          <button
            @click="() => { goProfile(); mobileMenuOpen = false }"
            class="flex items-center gap-2 text-gray-700"
          >
            <User class="w-5 h-5" />
            Профиль
          </button>

          <div class="pt-3 mt-3 border-t border-gray-200">
            <span class="px-3 py-1 bg-violet-50 text-violet-700 rounded-lg inline-block">Баланс: {{ userBalance }} ₽</span>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>
