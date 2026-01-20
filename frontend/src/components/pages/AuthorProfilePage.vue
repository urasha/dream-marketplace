<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User } from 'lucide-vue-next'
import LotCard from '../cards/LotCard.vue'
import { fetchUserProfile, fetchUserLots, fetchUserDreams, fetchUserPurchases } from '../../api/users'
import { API_BASE } from '../../api/httpClient'
import { normalizeErrorMessage } from '../../ui/feedback'

const route = useRoute()
const router = useRouter()

const userId = computed(() => Number(route.params.id) || null)

const profile = ref(null)
const lots = ref([])
const dreams = ref([])
const purchases = ref([])

const loading = ref(false)
const error = ref('')
const activeTab = ref('lots')

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

const loadAll = async () => {
  if (!userId.value) return
  loading.value = true
  error.value = ''
  try {
    const [profileData, lotsData, dreamsData, purchasesData] = await Promise.all([
      fetchUserProfile(userId.value),
      fetchUserLots(userId.value),
      fetchUserDreams(userId.value),
      fetchUserPurchases(userId.value),
    ])
    profile.value = profileData
    lots.value = Array.isArray(lotsData) ? lotsData : []
    dreams.value = Array.isArray(dreamsData) ? dreamsData : []
    purchases.value = Array.isArray(purchasesData) ? purchasesData : []
  } catch (e) {
    error.value = normalizeErrorMessage(e, 'Не удалось загрузить профиль автора')
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)
watch(userId, loadAll)
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <button
      @click="router.back()"
      class="flex items-center gap-2 mb-6 text-gray-600 hover:text-black transition-colors"
    >
      ← Назад
    </button>

    <div v-if="loading" class="text-gray-600">Загрузка...</div>
    <div v-else-if="error" class="text-red-600">{{ error }}</div>
    <template v-else>
      <div class="flex items-center gap-4 mb-8">
        <div class="w-12 h-12 rounded-full bg-violet-100 flex items-center justify-center">
          <User class="w-6 h-6 text-violet-600" />
        </div>
        <div>
          <h1 class="text-2xl font-semibold">{{ profile?.username || 'Автор' }}</h1>
          <div class="text-gray-500 text-sm">ID: {{ profile?.id || '—' }}</div>
        </div>
      </div>

      <div class="flex gap-2 mb-8 border-b border-gray-200">
        <button
          @click="activeTab = 'lots'"
          class="px-6 py-3 transition-colors relative"
          :class="activeTab === 'lots' ? 'text-violet-600' : 'text-gray-600 hover:text-gray-900'"
        >
          Лоты автора ({{ lots.length }})
          <div v-if="activeTab === 'lots'" class="absolute bottom-0 left-0 right-0 h-0.5 bg-violet-600" />
        </button>
        <button
          @click="activeTab = 'dreams'"
          class="px-6 py-3 transition-colors relative"
          :class="activeTab === 'dreams' ? 'text-violet-600' : 'text-gray-600 hover:text-gray-900'"
        >
          Сны автора ({{ dreams.length }})
          <div v-if="activeTab === 'dreams'" class="absolute bottom-0 left-0 right-0 h-0.5 bg-violet-600" />
        </button>
        <button
          @click="activeTab = 'purchases'"
          class="px-6 py-3 transition-colors relative"
          :class="activeTab === 'purchases' ? 'text-violet-600' : 'text-gray-600 hover:text-gray-900'"
        >
          Покупки автора ({{ purchases.length }})
          <div v-if="activeTab === 'purchases'" class="absolute bottom-0 left-0 right-0 h-0.5 bg-violet-600" />
        </button>
      </div>

      <div v-if="activeTab === 'lots'">
        <div v-if="!lots.length" class="text-gray-600">Лотов пока нет</div>
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <LotCard
            v-for="lot in lots"
            :key="lot.id"
            :title="lot.title"
            :description="lot.description"
            :price="lot.price"
            :author="lot.authorName"
            :author-id="lot.authorId"
            :category="lot.categoryName"
            :image-url="lot.visualizationUrl"
            :rating-average="lot.ratingAverage"
            :rating-count="lot.ratingCount"
            :tags="lot.tags || []"
            @click="() => router.push({ name: 'lot-detail', params: { id: lot.id } })"
            @author-click="(authorId) => router.push({ name: 'author-profile', params: { id: authorId } })"
          />
        </div>
      </div>

      <div v-else-if="activeTab === 'dreams'">
        <div v-if="!dreams.length" class="text-gray-600">Публичных снов нет</div>
        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div v-for="dream in dreams" :key="dream.id" class="p-6 bg-white border border-gray-200 rounded-xl">
            <div class="text-sm text-gray-500 mb-2">{{ formatDate(dream.createdAt) }}</div>
            <h3 class="mb-2">{{ dream.title }}</h3>
            <p class="text-gray-700 line-clamp-3">{{ dream.content }}</p>
          </div>
        </div>
      </div>

      <div v-else>
        <div v-if="!purchases.length" class="text-gray-600">Покупок пока нет</div>
        <div v-else class="space-y-4">
          <button
            v-for="purchase in purchases"
            :key="purchase.id"
            class="w-full text-left p-6 bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-shadow"
            @click="router.push({ name: 'lot-detail', params: { id: purchase.lotId } })"
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
    </template>
  </div>
</template>
