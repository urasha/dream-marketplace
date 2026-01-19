<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Plus } from 'lucide-vue-next'
import DreamCard from '../cards/DreamCard.vue'
import { useSessionStore } from '../../stores/session'
import { useDreamsStore } from '../../stores/dreams'
import { useLotsStore } from '../../stores/lots'
import { fetchPurchases } from '../../api/purchases'
import { API_BASE } from '../../api/httpClient'

const props = defineProps({
  userBalance: { type: Number, required: true },
})

const router = useRouter()
const route = useRoute()

const session = useSessionStore()
const dreamsStore = useDreamsStore()
const lotsStore = useLotsStore()

const activeTab = ref(['lots', 'purchases', 'dreams'].includes(route.query.tab) ? route.query.tab : 'dreams')
const updateStatus = ref('idle')
const updateError = ref('')

const tabs = [
  { id: 'dreams', label: 'Мои сны' },
  { id: 'lots', label: 'Мои лоты' },
  { id: 'purchases', label: 'Мои покупки' },
]

const displayName = computed(() => session.state.profile?.username || '—')
const displayEmail = computed(() => session.state.profile?.email || '—')

const usernameInput = ref('')
const emailInput = ref('')

const purchases = ref([])
const purchasesLoading = ref(false)
const purchasesError = ref('')

const dreams = computed(() => dreamsStore.state.items)
const dreamsLoading = computed(() => dreamsStore.state.loading)
const profileLoading = computed(() => session.state.loading)
const myLots = computed(() => lotsStore.state.mine)
const purchasesCount = computed(() => purchases.value.length)

const lotStatusLabels = {
  OPEN: 'Открыт',
  PENDING: 'На модерации',
  SOLD: 'Продан',
  CLOSED: 'Закрыт',
}

const lotStatusClass = {
  OPEN: 'bg-green-100 text-green-700',
  PENDING: 'bg-yellow-100 text-yellow-700',
  SOLD: 'bg-blue-100 text-blue-700',
  CLOSED: 'bg-gray-200 text-gray-700',
}

const formatLotStatus = (status) => lotStatusLabels[status] || status

const formatDate = (value) => {
  if (!value) return ''
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString('ru-RU')
}

const resolvePreviewUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
    return url
  }
  if (url.startsWith('/')) {
    return `${API_BASE}${url}`
  }
  return url
}

const dreamCards = computed(() =>
  dreams.value.map((dream) => ({
    id: dream.id,
    title: dream.title,
    date: formatDate(dream.createdAt),
    tags: [],
    isPrivate: dream.privacy === 'PRIVATE',
  }))
)

const loadPurchases = async () => {
  purchasesLoading.value = true
  purchasesError.value = ''
  try {
    purchases.value = await fetchPurchases()
  } catch (err) {
    purchases.value = []
    purchasesError.value = err?.data?.message || 'Не удалось загрузить покупки'
  } finally {
    purchasesLoading.value = false
  }
}

onMounted(async () => {
  if (!session.state.profile) {
    await session.loadProfile().catch(() => {})
  }
  usernameInput.value = session.state.profile?.username || ''
  emailInput.value = session.state.profile?.email || ''
  await dreamsStore.loadDreams().catch(() => {})
  await lotsStore.loadMyLots().catch(() => {})
  await loadPurchases()
})

const handleUpdateProfile = async () => {
  updateStatus.value = 'idle'
  updateError.value = ''
  const payload = {
    username: usernameInput.value.trim() || undefined,
    email: emailInput.value.trim() || undefined,
  }
  if (!payload.username && !payload.email) {
    updateStatus.value = 'error'
    updateError.value = 'Укажите имя или email'
    return
  }
  updateStatus.value = 'saving'
  try {
    await session.updateProfile(payload)
    updateStatus.value = 'success'
  } catch (err) {
    updateStatus.value = 'error'
    updateError.value = err?.data?.message || 'Не удалось обновить профиль'
  }
}

const handleLogout = async () => {
  await session.logout()
  router.push({ name: 'home' })
}
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <h1 class="mb-8 page-title">Профиль</h1>

    <div class="mb-8 p-6 bg-white rounded-xl border border-gray-200 shadow-sm">
      <div class="flex items-start gap-6">
        <div class="w-24 h-24 bg-gradient-to-br from-violet-500 to-indigo-500 rounded-xl flex items-center justify-center flex-shrink-0">
          <User class="w-12 h-12 text-white" />
        </div>
        <div class="flex-1">
          <div class="flex items-start justify-between gap-3 mb-4">
            <div class="flex items-center gap-3">
              <h2 class="text-xl font-semibold">{{ displayName }}</h2>
              <span v-if="profileLoading" class="text-sm text-gray-500">Загрузка...</span>
            </div>
            <button
              @click="handleLogout"
              class="px-4 py-2 border border-red-200 bg-red-50 text-sm font-medium text-red-700 rounded-lg hover:bg-red-100 hover:border-red-300 hover:text-red-800 transition-colors shadow-[0_6px_16px_rgba(239,68,68,0.18)]"
            >
              Выйти
            </button>
          </div>
          <div class="text-gray-700 mb-4">{{ displayEmail }}</div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <div class="text-gray-600 flex items-center gap-2">
                Баланс
                <button
                  @click="router.push({ name: 'wallet' })"
                  class="px-2 py-1 text-xs bg-violet-50 text-violet-700 rounded border border-violet-200 hover:border-violet-400 hover:bg-violet-100 transition-colors"
                >
                  Пополнить
                </button>
              </div>
              <div class="text-violet-600">{{ userBalance }} ₽</div>
            </div>
            <div>
              <div class="text-gray-600">Лотов создано</div>
              <div>{{ myLots.length }}</div>
            </div>
            <div>
              <div class="text-gray-600">Покупок</div>
              <div>{{ purchasesCount }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="mb-8 p-6 bg-white rounded-xl border border-gray-200 shadow-sm">
      <h3 class="mb-4">Редактирование профиля</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block mb-2 text-gray-700">Имя</label>
          <input
            v-model="usernameInput"
            type="text"
            class="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent"
            :disabled="profileLoading"
          />
        </div>
        <div>
          <label class="block mb-2 text-gray-700">Email</label>
          <input
            v-model="emailInput"
            type="email"
            class="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent"
            :disabled="profileLoading"
          />
        </div>
      </div>

      <div class="flex items-center gap-3 mt-4">
        <button
          @click="handleUpdateProfile"
          class="px-6 py-3 bg-violet-600 text-white rounded-lg hover:bg-violet-700 transition-colors disabled:opacity-50"
          :disabled="profileLoading || updateStatus === 'saving'"
        >
          {{ updateStatus === 'saving' ? 'Сохраняем...' : 'Сохранить' }}
        </button>
        <span v-if="updateStatus === 'success'" class="text-green-600">Сохранено</span>
        <span v-if="updateStatus === 'error'" class="text-red-600">{{ updateError }}</span>
      </div>
    </div>

    <div class="mb-6">
      <div class="flex gap-2 border-b border-gray-200">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          @click="activeTab = tab.id"
          class="px-6 py-3 transition-colors relative"
          :class="activeTab === tab.id ? 'text-violet-600' : 'text-gray-600 hover:text-gray-900'"
        >
          {{ tab.label }}
          <div v-if="activeTab === tab.id" class="absolute bottom-0 left-0 right-0 h-0.5 bg-violet-600" />
        </button>
      </div>
    </div>

    <div v-if="activeTab === 'dreams'">
      <div class="flex justify-end mb-6">
        <button
          @click="router.push({ name: 'create-dream' })"
          class="flex items-center gap-2 px-4 py-2 bg-violet-600 text-white rounded-lg hover:bg-violet-700 transition-colors"
        >
          <Plus class="w-4 h-4" />
          Создать запись
        </button>
      </div>
      <div v-if="dreamsLoading" class="text-gray-600">Загружаем сны...</div>
      <div v-else-if="dreamCards.length === 0" class="text-gray-600">Сны пока не созданы</div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <DreamCard
          v-for="dream in dreamCards"
          :key="dream.id"
          v-bind="dream"
          @click="router.push({ name: 'dream-detail', params: { id: dream.id } })"
        />
      </div>
    </div>

    <div v-else-if="activeTab === 'lots'">
      <div v-if="lotsStore.state.loading" class="text-gray-600">Загружаем лоты...</div>
      <div v-else-if="myLots.length === 0" class="text-gray-600">Лоты пока не созданы</div>
      <div v-else class="space-y-4">
        <button
          v-for="lot in myLots"
          :key="lot.id"
          class="w-full text-left p-6 bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-shadow"
          @click="router.push({ name: 'lot-detail', params: { id: lot.id }, query: { from: 'profile-lots' } })"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <h3 class="mb-3">{{ lot.title }}</h3>
              <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
                <div>
                  <div class="text-gray-600">Цена</div>
                  <div>{{ lot.price }} ₽</div>
                </div>
                <div>
                  <div class="text-gray-600">Статус</div>
                  <span
                    class="inline-block px-3 py-1 rounded-full"
                    :class="lotStatusClass[lot.status] || 'bg-gray-100 text-gray-700'"
                  >
                    {{ formatLotStatus(lot.status) }}
                  </span>
                </div>
                <div>
                  <div class="text-gray-600">Дата</div>
                  <div>{{ lot.submittedAt }}</div>
                </div>
              </div>
            </div>
            <span class="text-violet-600">Открыть →</span>
          </div>
        </button>
      </div>
    </div>

    <div v-else>
      <div v-if="purchasesLoading" class="text-gray-600">Загружаем покупки...</div>
      <div v-else-if="purchasesError" class="text-red-600">{{ purchasesError }}</div>
      <div v-else-if="purchases.length === 0" class="text-gray-600">Покупок пока нет</div>
      <div v-else class="space-y-4">
        <button
          v-for="purchase in purchases"
          :key="purchase.id"
          class="w-full text-left p-6 bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-shadow"
          @click="router.push({ name: 'lot-detail', params: { id: purchase.lotId }, query: { from: 'profile-purchases' } })"
        >
          <div class="flex items-start gap-4">
            <div class="w-24 h-24 rounded-lg overflow-hidden bg-gray-100 border border-gray-200 flex items-center justify-center">
              <img
                v-if="purchase.visualizationUrl"
                :src="resolvePreviewUrl(purchase.visualizationUrl)"
                alt="Визуализация"
                class="w-full h-full object-cover"
              />
              <span v-else class="text-xs text-gray-400">Нет превью</span>
            </div>
            <div class="flex-1">
              <h3 class="mb-2">{{ purchase.lotTitle }}</h3>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-3 text-sm">
                <div>
                  <div class="text-gray-600">Автор</div>
                  <div>{{ purchase.authorName || '—' }}</div>
                </div>
                <div>
                  <div class="text-gray-600">Цена</div>
                  <div>{{ purchase.amount }} ₽</div>
                </div>
                <div>
                  <div class="text-gray-600">Дата покупки</div>
                  <div>{{ formatDate(purchase.purchasedAt) }}</div>
                </div>
              </div>
            </div>
            <span class="text-violet-600">Открыть →</span>
          </div>
        </button>
      </div>
    </div>
  </div>
</template>
