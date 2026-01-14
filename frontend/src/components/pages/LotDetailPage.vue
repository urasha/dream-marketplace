<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Download } from 'lucide-vue-next'
import { useLotsStore } from '../../stores/lots'

const props = defineProps({
  lotId: { type: Number, default: null },
})

const router = useRouter()
const route = useRoute()
const lotsStore = useLotsStore()

const isPurchased = ref(false)
const loading = computed(() => lotsStore.state.loading)
const lot = computed(() => lotsStore.state.current)

const backTarget = computed(() => {
  if (route.query.from === 'profile-lots') {
    return { name: 'profile', query: { tab: 'lots' } }
  }
  return { name: 'home' }
})

onMounted(() => {
  if (props.lotId) {
    lotsStore.loadLot(props.lotId).catch(() => {})
  }
})
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <button
      @click="router.push(backTarget)"
      class="flex items-center gap-2 mb-6 text-gray-600 hover:text-black transition-colors"
    >
      <ArrowLeft class="w-5 h-5" />
      Назад
    </button>

    <div v-if="loading" class="text-gray-600">Загрузка...</div>
    <template v-else-if="lot">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 mb-12">
        <div>
          <div class="w-full aspect-[4/3] bg-gray-200 border-2 border-gray-300 flex items-center justify-center mb-4">
            <div class="text-gray-400">Визуализация</div>
          </div>
        </div>

        <div>
          <h1 class="mb-4">{{ lot.title }}</h1>
          <div class="text-gray-600 mb-4">Автор: {{ lot.authorName || '—' }}</div>
          <div class="mb-6 text-xl font-semibold">{{ lot.price }} ₽</div>

          <div class="p-6 bg-gray-50 border-2 border-gray-300 mb-6">
            <h3 class="mb-3">Описание</h3>
            <p class="text-gray-700">{{ lot.description || 'Описание не указано' }}</p>
          </div>

          <button
            @click="router.push({ name: 'purchase', params: { id: lot.id } })"
            class="w-full py-4 bg-black text-white hover:bg-gray-800 transition-colors"
          >
            Купить
          </button>
        </div>
      </div>

      <div v-if="isPurchased" class="p-8 bg-green-50 border-2 border-green-600 mb-8">
        <h2 class="mb-4">Вы владеете этим лотом</h2>
        <p class="text-gray-700 mb-6">Теперь вы можете скачать файл.</p>

        <div class="p-6 bg-white border-2 border-gray-300 mb-4">
          <div class="flex items-center justify-between">
            <div>
              <div class="mb-1">{{ lot.title }}.asset</div>
              <div class="text-gray-600">Файл доступен для скачивания</div>
            </div>
            <button class="px-6 py-3 bg-black text-white hover:bg-gray-800 transition-colors flex items-center gap-2">
              <Download class="w-5 h-5" />
              Скачать
            </button>
          </div>
        </div>
      </div>
    </template>

    <p v-else>Лот не найден</p>
  </div>
</template>
