<script setup>
import { ref, computed } from 'vue'
import { dreams } from '../../data/mockData'
import { ArrowLeft, Loader, CheckCircle } from 'lucide-vue-next'

const props = defineProps({
  dreamId: { type: Number, default: null },
})

const emit = defineEmits(['navigate'])

const generationStatus = ref('idle')
const selectedVisualization = ref(null)

const dream = computed(() => dreams.find((d) => d.id === props.dreamId))

const hasVisualizations = computed(() => dream.value?.visualizations?.length > 0)
const hasReadyVisualizations = computed(() => dream.value?.visualizations?.some((v) => v.status === 'ready'))

const handleRequestGeneration = () => {
  generationStatus.value = 'processing'
  setTimeout(() => {
    generationStatus.value = 'ready'
  }, 3000)
}
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <button
      @click="emit('navigate', 'profile')"
      class="flex items-center gap-2 mb-6 text-gray-600 hover:text-black transition-colors"
    >
      <ArrowLeft class="w-5 h-5" />
      Назад
    </button>

    <template v-if="dream">
      <div class="mb-8">
        <div class="flex items-start justify-between mb-4">
          <h1>{{ dream.title }}</h1>
          <div class="px-3 py-1 bg-gray-200 border border-gray-400">
            {{ dream.isPrivate ? 'Приватный' : 'Публичный' }}
          </div>
        </div>

        <div class="text-gray-600 mb-4">{{ dream.date }}</div>

        <div class="flex flex-wrap gap-2 mb-6">
          <span v-for="(tag, index) in dream.tags" :key="index" class="px-3 py-1 bg-gray-200 border border-gray-400">
            {{ tag }}
          </span>
        </div>

        <div class="whitespace-pre-line text-gray-800 leading-relaxed">
          {{ dream.fullText }}
        </div>
      </div>

      <div class="border-t-2 border-gray-300 pt-8">
        <h2 class="mb-6">Визуализации</h2>

        <div v-if="generationStatus === 'processing'" class="p-12 bg-gray-100 border-2 border-gray-300 flex flex-col items-center justify-center">
          <Loader class="w-12 h-12 animate-spin mb-4" />
          <h3 class="mb-2">Генерация в процессе</h3>
          <p class="text-gray-600">Создаём визуализации для вашего сна...</p>
          <div class="w-full max-w-md mt-6 bg-gray-300 h-2">
            <div class="bg-black h-full w-2/3 transition-all duration-300" />
          </div>
        </div>

        <div v-else-if="generationStatus === 'ready' || hasReadyVisualizations" class="space-y-6">
          <div class="flex items-center gap-3 p-4 bg-green-100 border-2 border-green-600">
            <CheckCircle class="w-6 h-6" />
            <div>
              <h3>Визуализации готовы</h3>
              <p class="text-gray-700">Выберите вариант для создания лота</p>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <button
              v-for="vizId in [1, 2, 3]"
              :key="vizId"
              @click="selectedVisualization = vizId"
              class="border-4 transition-colors"
              :class="selectedVisualization === vizId ? 'border-black' : 'border-gray-300 hover:border-gray-500'"
            >
              <div class="w-full aspect-[4/3] bg-gray-200 flex items-center justify-center">
                <div class="text-center">
                  <div class="text-gray-400 mb-2">400×300</div>
                  <div class="text-gray-600">viz_{{ vizId }}</div>
                </div>
              </div>
            </button>
          </div>

          <button
            v-if="selectedVisualization"
            @click="emit('navigate', 'create-lot')"
            class="w-full md:w-auto px-8 py-3 bg-black text-white hover:bg-gray-800 transition-colors"
          >
            Создать лот из выбранной визуализации
          </button>
        </div>

        <div v-else class="p-12 bg-gray-100 border-2 border-gray-300 text-center">
          <h3 class="mb-4">Визуализации не созданы</h3>
          <p class="text-gray-600 mb-6">Запросите генерацию визуализаций на основе описания вашего сна</p>
          <button
            @click="handleRequestGeneration"
            class="px-8 py-3 bg-black text-white hover:bg-gray-800 transition-colors"
          >
            Запросить генерацию
          </button>
        </div>
      </div>
    </template>

    <p v-else>Сон не найден</p>
  </div>
</template>
