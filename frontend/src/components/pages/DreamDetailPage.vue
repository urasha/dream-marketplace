<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Loader, CheckCircle } from 'lucide-vue-next'
import { useDreamsStore } from '../../stores/dreams'
import { createImageGeneration, fetchImageGeneration } from '../../api/images'
import { attachVisualization } from '../../api/dreams'
import { imageGenConfig } from '../../config/imageGen'

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
const genTaskId = ref(null)
const genStatus = ref('')
const genResultUrl = ref('')
const genResults = ref([])
const selectedResult = ref('')
const pollCount = ref(0)
const isRequesting = ref(false)
const maxPolls = imageGenConfig.maxPolls || 60
const pollIntervalMs = imageGenConfig.pollIntervalMs || 2000
let pollTimer = null

const statusLabels = {
  PENDING: 'В очереди на генерацию',
  PROCESSING: 'В процессе',
  RUNNING: 'В процессе',
  DONE: 'Готово',
  READY: 'Готово',
  ACCEPTED: 'Принято',
  FAILED: 'Ошибка',
  ERROR: 'Ошибка',
}

const genStatusLabel = computed(() => statusLabels[genStatus.value] || genStatus.value)
const formatStatus = (status) => statusLabels[status] || status

const hasVisualizations = computed(() => visualizations.value.length > 0)
const readyVisualizations = computed(() => visualizations.value.filter((v) => v.status === 'READY' || v.status === 'ACCEPTED'))
const isMock = computed(() => (imageGenConfig.mode || 'mock') === 'mock')

const previewSrc = ref(null)

const storageKey = computed(() => (dream.value ? `dream-gen:${dream.value.id}` : null))

const openPreview = (url) => {
  if (!url) return
  previewSrc.value = url
}

const closePreview = () => {
  previewSrc.value = null
}

const handleKey = (e) => {
  if (e.key === 'Escape') {
    closePreview()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKey)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKey)
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})

const saveGenState = (state) => {
  if (!storageKey.value) return
  try {
    const payload = {
      taskId: genTaskId.value,
      status: genStatus.value,
      results: genResults.value,
      selected: selectedResult.value,
      pollCount: pollCount.value,
      ...state,
    }
    localStorage.setItem(storageKey.value, JSON.stringify(payload))
  } catch (e) {
    /* ignore */
  }
}

const loadGenState = () => {
  if (!storageKey.value) return null
  try {
    const raw = localStorage.getItem(storageKey.value)
    if (!raw) return null
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

const clearGenState = () => {
  if (!storageKey.value) return
  localStorage.removeItem(storageKey.value)
}

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
      restoreGenerationState()
    }
  } catch (err) {
    requestError.value = err?.data?.message || 'Не удалось загрузить данные'
  } finally {
    loading.value = false
    visLoading.value = false
  }
}

const restoreGenerationState = () => {
  const saved = loadGenState()
  if (!saved) return
  genTaskId.value = saved.taskId || null
  genStatus.value = saved.status || ''
  genResults.value = saved.results || []
  selectedResult.value = saved.selected || ''
  pollCount.value = saved.pollCount || 0

  if (genStatus.value && genStatus.value !== 'DONE' && genStatus.value !== 'FAILED' && genTaskId.value) {
    isRequesting.value = true
    if (!pollTimer) {
      pollTimer = setInterval(checkGenerationStatus, pollIntervalMs)
    }
  }
}

const handleRequestGeneration = async () => {
  if (!dream.value) return
  requestError.value = ''
  genStatus.value = ''
  genResultUrl.value = ''
  genResults.value = []
  selectedResult.value = ''
  pollCount.value = 0

  clearGenState()

  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }

  if (isMock.value) {
    genStatus.value = 'DONE'
    genResults.value = Array.from({ length: 4 }, (_, i) => `${imageGenConfig.mockUrl}?v=${i + 1}`)
    selectedResult.value = genResults.value[0]
    saveGenState({})
    return
  }

  isRequesting.value = true
  try {
    const payload = { prompt: dream.value.content || '' }
    const resp = await createImageGeneration(payload)
    genStatus.value = resp?.status || 'PENDING'
    genTaskId.value = resp?.id || null
    saveGenState({})

    if (!genTaskId.value) {
      throw new Error('Не получили id задачи генерации')
    }

    pollTimer = setInterval(checkGenerationStatus, pollIntervalMs)
  } catch (err) {
    requestError.value = err?.data?.message || err.message || 'Не удалось запросить визуализацию'
    isRequesting.value = false
  }
}

const persistVisualization = async (filePath, mime) => {
  if (!dream.value) return null
  try {
    const saved = await attachVisualization(dream.value.id, {
      filePath,
      mime: mime || 'image/png',
      generator: 'ImagesAPI',
    })
    return saved
  } catch (err) {
    requestError.value = requestError.value || err?.data?.message || 'Не удалось сохранить визуализацию'
    return {
      id: `local-${Date.now()}`,
      status: 'READY',
      filePath,
      mime: mime || 'image/png',
    }
  }
}

const selectResult = (url) => {
  selectedResult.value = url
  saveGenState({})
}

const saveSelectedResult = async () => {
  requestError.value = ''
  if (!selectedResult.value) {
    requestError.value = 'Выберите вариант изображения'
    return
  }
  const saved = await persistVisualization(selectedResult.value, 'image/png')
  if (saved) {
    visualizations.value = [saved, ...visualizations.value]
  }
  clearGenState()
  genResults.value = []
  selectedResult.value = ''
  genTaskId.value = null
  genStatus.value = ''
  genResultUrl.value = ''
}

const checkGenerationStatus = async () => {
  if (!genTaskId.value) return
  if (pollCount.value >= maxPolls) {
    requestError.value = 'Таймаут ожидания результата'
    isRequesting.value = false
    clearInterval(pollTimer)
    pollTimer = null
    return
  }

  pollCount.value += 1
  try {
    const resp = await fetchImageGeneration(genTaskId.value)
    genStatus.value = resp?.status || ''
    if (Array.isArray(resp?.resultUrls) && resp.resultUrls.length) {
      genResults.value = resp.resultUrls
    } else if (resp?.resultUrl) {
      genResults.value = [resp.resultUrl]
    }
    saveGenState({})

    if (resp?.status === 'DONE') {
      genResultUrl.value = resp.resultUrl || ''
      if (!genResults.value.length && genResultUrl.value) {
        genResults.value = [genResultUrl.value]
      }
      saveGenState({ status: 'DONE' })
      isRequesting.value = false
      clearInterval(pollTimer)
      pollTimer = null
    }

    if (resp?.status === 'FAILED') {
      requestError.value = resp?.error || 'Генерация завершилась ошибкой'
      isRequesting.value = false
      clearInterval(pollTimer)
      pollTimer = null
      saveGenState({ status: 'FAILED' })
    }
  } catch (err) {
    requestError.value = err?.data?.message || err.message || 'Ошибка при получении статуса'
    isRequesting.value = false
    clearInterval(pollTimer)
    pollTimer = null
    saveGenState({})
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
        <div class="flex items-center gap-3 mb-4 flex-wrap">
          <h2>Визуализации</h2>
          <Loader v-if="visLoading || isRequesting" class="w-5 h-5 animate-spin text-gray-500" />
          <span v-if="genStatus" class="text-gray-700 text-sm">Статус генерации: {{ genStatusLabel }}</span>
          <span v-if="requestError" class="text-red-600">{{ requestError }}</span>
          <button
            @click="handleRequestGeneration"
            class="ml-auto px-4 py-2 bg-black text-white rounded-lg hover:bg-gray-800 transition-colors disabled:opacity-60"
            :disabled="isRequesting"
          >
            {{ isRequesting ? 'Генерируем...' : 'Запросить генерацию' }}
          </button>
        </div>

        <div v-if="hasVisualizations" class="space-y-4">
          <div
            v-for="viz in visualizations"
            :key="viz.id"
            class="p-4 border border-gray-200 rounded-lg flex flex-col gap-4 md:flex-row md:items-center md:justify-between"
          >
            <div class="flex items-start gap-4">
              <div
                v-if="viz.filePath || isMock"
                class="w-24 h-24 rounded border overflow-hidden bg-gray-100 flex-shrink-0 cursor-pointer"
                @click="openPreview(viz.filePath || imageGenConfig.mockUrl)"
              >
                <img
                  :src="viz.filePath || imageGenConfig.mockUrl"
                  alt="viz"
                  class="w-full h-full object-cover transition-transform duration-200 hover:scale-105"
                />
              </div>
              <div>
                <div class="font-medium">Визуализация</div>
                <div class="text-gray-600 text-sm">Статус: {{ formatStatus(viz.status) }}</div>
                <div v-if="viz.mime && viz.mime !== 'text/plain'" class="text-gray-600 text-sm">{{ viz.mime }}</div>
              </div>
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

        <div v-if="genResults.length" class="mt-6 space-y-3">
          <div class="flex items-center justify-between">
            <div class="font-medium">Результаты генерации</div>
            <div class="text-sm text-gray-600">Выберите вариант и сохраните</div>
          </div>
          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
            <button
              v-for="url in genResults"
              :key="url"
              type="button"
              class="relative border rounded-lg overflow-hidden bg-gray-100 focus:outline-none focus:ring-2 focus:ring-black"
              :class="selectedResult === url ? 'border-black' : 'border-gray-200'"
              @click="selectResult(url)"
            >
              <img :src="url" alt="generated option" class="w-full h-36 object-cover" />
              <div
                v-if="selectedResult === url"
                class="absolute inset-0 bg-black/30 text-white flex items-center justify-center text-sm font-semibold"
              >
                Выбрано
              </div>
            </button>
          </div>
          <div class="flex items-center gap-3">
            <button
              class="px-4 py-2 bg-black text-white rounded-lg hover:bg-gray-800 disabled:opacity-60"
              :disabled="!selectedResult"
              @click="saveSelectedResult"
            >
              Сохранить выбранное
            </button>
          </div>
        </div>

        <div v-if="readyVisualizations.length" class="mt-6 p-4 bg-green-50 border border-green-200 rounded-lg flex items-center gap-3">
          <CheckCircle class="w-5 h-5 text-green-600" />
          <div class="text-gray-700">Есть готовые визуализации, выберите любую для создания лота.</div>
        </div>
      </div>
    </template>

    <p v-else>Сон не найден</p>
  </div>

  <Transition name="lightbox">
    <div
      v-if="previewSrc"
      class="lightbox-overlay"
      @click="closePreview"
    >
      <img
        :src="previewSrc"
        alt="preview"
        class="lightbox-image"
        @click.stop
      />
    </div>
  </Transition>
</template>

<style>
.lightbox-enter-active,
.lightbox-leave-active {
  transition: opacity 200ms ease;
}

.lightbox-enter-from,
.lightbox-leave-to {
  opacity: 0;
}

.lightbox-enter-to,
.lightbox-leave-from {
  opacity: 1;
}
.lightbox-overlay {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.78);
  backdrop-filter: blur(2px);
}

.lightbox-image {
  max-width: 90vw;
  max-height: 90vh;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.35);
  transition: transform 200ms ease, opacity 200ms ease;
}

.lightbox-enter-from .lightbox-image,
.lightbox-leave-to .lightbox-image {
  opacity: 0;
  transform: scale(0.95);
}

.lightbox-enter-to .lightbox-image,
.lightbox-leave-from .lightbox-image {
  opacity: 1;
  transform: scale(1);
}
</style>
