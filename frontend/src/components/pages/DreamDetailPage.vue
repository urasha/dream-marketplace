<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Loader, CheckCircle } from 'lucide-vue-next'
import { useDreamsStore } from '../../stores/dreams'

const props = defineProps({
  dreamId: { type: Number, default: null },
})

const router = useRouter()
const dreamsStore = useDreamsStore()

const dream = ref(null)
const visualizations = ref([])
const loading = ref(true)
const visLoading = ref(false)
const requestError = ref('')
const selectedVisualization = ref(null)

const hasVisualizations = computed(() => visualizations.value.length > 0)
const readyVisualizations = computed(() => visualizations.value.filter((v) => v.status === 'READY' || v.status === 'ACCEPTED'))

const loadDream = async () => {
  loading.value = true
  requestError.value = ''
  try {
    if (dreamsStore.state.items.length === 0) {
      await dreamsStore.loadDreams()
    }
    dream.value = dreamsStore.state.items.find((d) => d.id === props.dreamId) || null
    if (dream.value) {
      visLoading.value = true
      visualizations.value = await dreamsStore.loadVisualizations(dream.value.id)
    }
  } catch (err) {
    requestError.value = err?.data?.message || 'Не удалось загрузить данные'
  } finally {
    loading.value = false
    visLoading.value = false
  }
}

const handleRequestGeneration = async () => {
  if (!dream.value) return
  requestError.value = ''
  visLoading.value = true
  try {
    const created = await dreamsStore.requestDreamVisualization(dream.value.id)
    visualizations.value = [created, ...visualizations.value]
  } catch (err) {
    requestError.value = err?.data?.message || 'Не удалось запросить визуализацию'
  } finally {
    visLoading.value = false
  }
}

onMounted(loadDream)
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <button
      @click="router.push({ name: 'profile' })"
      class="flex items-center gap-2 mb-6 text-gray-600 hover:text-black transition-colors"
    >
      <ArrowLeft class="w-5 h-5" />
      Назад
    </button>

    <div v-if="loading" class="text-gray-600">Загрузка...</div>
    <template v-else-if="dream">
      <div class="mb-8">
        <div class="flex items-start justify-between mb-4">
          <h1>{{ dream.title }}</h1>
          <div class="px-3 py-1 bg-gray-200 border border-gray-400">
            {{ dream.privacy === 'PRIVATE' ? 'Приватный' : 'Публичный' }}
          </div>
        </div>

        <div class="text-gray-600 mb-4">{{ dream.createdAt }}</div>

        <div class="whitespace-pre-line text-gray-800 leading-relaxed">
          {{ dream.content }}
        </div>
      </div>

      <div class="border-t-2 border-gray-300 pt-8">
        <div class="flex items-center gap-3 mb-4">
          <h2>Визуализации</h2>
          <Loader v-if="visLoading" class="w-5 h-5 animate-spin text-gray-500" />
          <span v-if="requestError" class="text-red-600">{{ requestError }}</span>
        </div>

        <div v-if="hasVisualizations" class="space-y-4">
          <div
            v-for="viz in visualizations"
            :key="viz.id"
            class="p-4 border border-gray-200 rounded-lg flex items-center justify-between"
          >
            <div>
              <div class="font-medium">Визуализация #{{ viz.id }}</div>
              <div class="text-gray-600 text-sm">Статус: {{ viz.status }}</div>
              <div v-if="viz.mime" class="text-gray-600 text-sm">{{ viz.mime }}</div>
            </div>
            <button
              v-if="viz.status === 'READY' || viz.status === 'ACCEPTED'"
              @click="selectedVisualization = viz.id; router.push({ name: 'create-lot', query: { dreamId: dream.id, visualizationId: viz.id } })"
              class="px-4 py-2 bg-black text-white rounded-lg hover:bg-gray-800 transition-colors"
            >
              Создать лот
            </button>
          </div>
        </div>

        <div v-else class="p-12 bg-gray-100 border-2 border-gray-300 text-center">
          <h3 class="mb-4">Визуализации не созданы</h3>
          <p class="text-gray-600 mb-6">Запросите генерацию визуализаций на основе описания вашего сна</p>
          <button
            @click="handleRequestGeneration"
            class="px-8 py-3 bg-black text-white hover:bg-gray-800 transition-colors"
            :disabled="visLoading"
          >
            {{ visLoading ? 'Отправляем...' : 'Запросить генерацию' }}
          </button>
        </div>

        <div v-if="readyVisualizations.length" class="mt-6 p-4 bg-green-50 border border-green-200 rounded-lg flex items-center gap-3">
          <CheckCircle class="w-5 h-5 text-green-600" />
          <div class="text-gray-700">Есть готовые визуализации, выберите любую для создания лота.</div>
        </div>
      </div>
    </template>

    <p v-else>Сон не найден</p>
  </div>
</template>
