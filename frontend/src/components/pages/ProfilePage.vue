<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Plus } from 'lucide-vue-next'
import { userLots, userTransactions } from '../../data/mockData'
import DreamCard from '../cards/DreamCard.vue'
import { useSessionStore } from '../../stores/session'
import { useDreamsStore } from '../../stores/dreams'

const props = defineProps({
  userBalance: { type: Number, required: true },
})

const router = useRouter()

const session = useSessionStore()
const dreamsStore = useDreamsStore()

const activeTab = ref('dreams')
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

const dreams = computed(() => dreamsStore.state.items)
const dreamsLoading = computed(() => dreamsStore.state.loading)
const profileLoading = computed(() => session.state.loading)

const formatDate = (value) => {
  if (!value) return ''
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString('ru-RU')
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

onMounted(async () => {
  if (!session.state.profile) {
    await session.loadProfile().catch(() => {})
  }
  usernameInput.value = session.state.profile?.username || ''
  emailInput.value = session.state.profile?.email || ''
  await dreamsStore.loadDreams().catch(() => {})
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
    <h1 class="mb-8">Профиль</h1>

    <div class="mb-8 p-6 bg-white rounded-xl border border-gray-200 shadow-sm">
      <div class="flex items-start gap-6">
        <div class="w-24 h-24 bg-gradient-to-br from-violet-500 to-indigo-500 rounded-xl flex items-center justify-center flex-shrink-0">
          <User class="w-12 h-12 text-white" />
        </div>
        <div class="flex-1">
          <div class="flex items-center gap-3 mb-4">
            <h2 class="text-xl font-semibold">{{ displayName }}</h2>
            <span v-if="profileLoading" class="text-sm text-gray-500">Загрузка...</span>
          </div>
          <div class="text-gray-700 mb-4">{{ displayEmail }}</div>

          <button
            @click="handleLogout"
            class="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:border-red-500 hover:text-red-600 transition-colors"
          >
            Выйти
          </button>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <div class="text-gray-600">Баланс</div>
              <div class="text-violet-600">{{ userBalance }} ₽</div>
            </div>
            <div>
              <div class="text-gray-600">Лотов создано</div>
              <div>{{ userLots.length }}</div>
            </div>
            <div>
              <div class="text-gray-600">Покупок</div>
              <div>{{ userTransactions.filter((t) => t.type === 'purchase').length }}</div>
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
      <div class="flex items-center justify-between mb-6">
        <h3>Мои записи снов</h3>
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
      <h3 class="mb-6">Мои лоты</h3>
      <div class="space-y-4">
        <div
          v-for="lot in userLots"
          :key="lot.id"
          class="p-6 bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-shadow"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <h3 class="mb-3">{{ lot.title }}</h3>
              <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div>
                  <div class="text-gray-600">Цена</div>
                  <div>{{ lot.price }} ₽</div>
                </div>
                <div>
                  <div class="text-gray-600">Статус</div>
                  <span
                    class="inline-block px-3 py-1 rounded-full"
                    :class="lot.status === 'published' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'"
                  >
                    {{ lot.status === 'published' ? 'Опубликован' : 'На модерации' }}
                  </span>
                </div>
                <div v-if="lot.sales > 0">
                  <div class="text-gray-600">Продаж</div>
                  <div>{{ lot.sales }}</div>
                </div>
                <div v-if="lot.sales > 0">
                  <div class="text-gray-600">Доход</div>
                  <div class="text-violet-600">{{ lot.revenue }} ₽</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else>
      <h3 class="mb-6">Мои покупки</h3>
      <div class="space-y-4">
        <div
          v-for="transaction in userTransactions"
          :key="transaction.id"
          class="p-6 bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-shadow"
        >
          <template v-if="transaction.type === 'purchase'">
            <div class="flex items-start justify-between mb-4">
              <div class="flex-1">
                <h3 class="mb-2">{{ transaction.lotTitle }}</h3>
                <div class="text-gray-600">{{ transaction.date }}</div>
              </div>
              <div class="text-right">
                <div class="text-gray-600">Сумма</div>
                <div>{{ transaction.amount }} ₽</div>
              </div>
            </div>
            <button
              @click="router.push({ name: 'lot-detail', params: { id: transaction.lotId } })"
              class="w-full md:w-auto px-6 py-2 bg-violet-600 text-white rounded-lg hover:bg-violet-700 transition-colors"
            >
              Открыть лот
            </button>
          </template>

          <template v-else>
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <h3 class="mb-2">Пополнение баланса</h3>
                <div class="text-gray-600">{{ transaction.date }}</div>
              </div>
              <div class="text-right">
                <div class="text-gray-600">Сумма</div>
                <div class="text-green-600">+{{ transaction.amount }} ₽</div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>
