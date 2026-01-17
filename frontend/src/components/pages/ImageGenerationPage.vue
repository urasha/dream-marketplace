<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { Loader, Sparkles } from 'lucide-vue-next'
import { createImageGeneration, fetchImageGeneration } from '../../api/images'
import { imageGenConfig } from '../../config/imageGen'
const prompt = ref('')
const aspectRatio = ref('1:1')
const modelVersion = ref('7.0')
const translateInput = ref(true)
const upgradePrompt = ref(false)

const status = ref('')
const error = ref('')
const resultUrl = ref('')
const taskId = ref(null)
const isRequesting = ref(false)
const pollCount = ref(0)

const mockImageUrl = imageGenConfig.mockUrl
const maxPolls = imageGenConfig.maxPolls || 60
let pollTimer = null
const isMock = computed(() => (imageGenConfig.mode || 'mock') === 'mock')

const resetState = () => {
  status.value = ''
  error.value = ''
  resultUrl.value = ''
  taskId.value = null
  pollCount.value = 0
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const startGeneration = async () => {
  resetState()
  if (!prompt.value.trim()) {
    error.value = 'Введите prompt для генерации'
    return
  }

  if (isMock.value) {
    status.value = 'DONE'
    resultUrl.value = mockImageUrl
    return
  }

  isRequesting.value = true
  try {
    const payload = {
      prompt: prompt.value.trim(),
      modelVersion: modelVersion.value || null,
      aspectRatio: aspectRatio.value || null,
      translateInput: translateInput.value,
      upgradePrompt: upgradePrompt.value,
    }
    const resp = await createImageGeneration(payload)
    status.value = resp?.status || 'PENDING'
    taskId.value = resp?.id || null

    if (!taskId.value) {
      throw new Error('Не получили id задачи генерации')
    }

    pollTimer = setInterval(checkStatus, 2500)
  } catch (err) {
    error.value = err?.data?.message || err.message || 'Не удалось запустить генерацию'
    isRequesting.value = false
  }
}

const checkStatus = async () => {
  if (!taskId.value) return
  if (pollCount.value >= maxPolls) {
    error.value = 'Таймаут ожидания результата'
    isRequesting.value = false
    clearInterval(pollTimer)
    pollTimer = null
    return
  }

  pollCount.value += 1
  try {
    const resp = await fetchImageGeneration(taskId.value)
    status.value = resp?.status || ''

    if (resp?.status === 'DONE') {
      resultUrl.value = resp.resultUrl || ''
      isRequesting.value = false
      clearInterval(pollTimer)
      pollTimer = null
    }
    if (resp?.status === 'FAILED') {
      error.value = resp?.error || 'Генерация завершилась ошибкой'
      isRequesting.value = false
      clearInterval(pollTimer)
      pollTimer = null
    }
  } catch (err) {
    error.value = err?.data?.message || err.message || 'Ошибка при получении статуса'
    isRequesting.value = false
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
  }
})
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12 space-y-8">
    <div class="flex flex-col gap-3">
      <div class="flex items-center gap-3">
        <Sparkles class="w-6 h-6 text-violet-600" />
        <h1>Генерация изображений</h1>
      </div>
      <p class="text-gray-700 leading-relaxed">
        Тестируем вывод: можно переключаться между mock-режимом (без обращения к реальному API) и настоящей генерацией на бэке.
        В реальном режиме результат забирается из S3, поэтому переключатель помогает экономить бюджет во время разработческих проверок.
      </p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
      <div class="lg:col-span-2 space-y-6">
        <div class="p-6 border border-gray-200 rounded-lg bg-white space-y-4">
          <label class="block text-sm font-medium text-gray-800">Prompt</label>
          <textarea
            v-model="prompt"
            rows="6"
            class="w-full border border-gray-200 rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-violet-500"
            placeholder="Опишите, что нужно сгенерировать"
          ></textarea>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-800 mb-1">Aspect ratio</label>
              <select
                v-model="aspectRatio"
                class="w-full border border-gray-200 rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-violet-500"
              >
                <option value="1:1">1:1</option>
                <option value="16:9">16:9</option>
                <option value="4:5">4:5</option>
                <option value="9:16">9:16</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-800 mb-1">Версия модели</label>
              <input
                v-model="modelVersion"
                type="text"
                class="w-full border border-gray-200 rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-violet-500"
              />
            </div>
            <div class="flex items-center gap-3">
              <label class="flex items-center gap-2 text-sm text-gray-800">
                <input type="checkbox" v-model="translateInput" />
                Переводить prompt
              </label>
              <label class="flex items-center gap-2 text-sm text-gray-800">
                <input type="checkbox" v-model="upgradePrompt" />
                Улучшать prompt
              </label>
            </div>
          </div>

          <div class="flex gap-3">
            <button
              @click="startGeneration"
              class="px-6 py-3 bg-black text-white rounded-lg hover:bg-gray-800 transition-colors flex items-center gap-2"
              :disabled="isRequesting && !isMock"
            >
              <Loader v-if="isRequesting && !isMock" class="w-4 h-4 animate-spin" />
              <span>Сгенерировать</span>
            </button>
            <button
              @click="resetState"
              class="px-4 py-3 border border-gray-200 rounded-lg hover:border-gray-300 text-gray-700"
            >
              Сбросить
            </button>
          </div>

          <div v-if="error" class="p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg">
            {{ error }}
          </div>
        </div>
      </div>

      <div class="space-y-4">
        <div class="p-4 border border-gray-200 rounded-lg bg-white">
          <div class="flex items-center justify-between mb-3">
            <div class="text-sm text-gray-700">Текущий статус</div>
            <Loader v-if="isRequesting && !isMock" class="w-4 h-4 animate-spin text-gray-500" />
          </div>
          <div class="text-lg font-semibold">
            {{ status || 'Нет активной задачи' }}
          </div>
          <div v-if="taskId" class="text-xs text-gray-500 mt-1">ID: {{ taskId }}</div>
          <div v-if="!isMock" class="text-xs text-gray-500 mt-1">Попыток опроса: {{ pollCount }} / {{ maxPolls }}</div>
        </div>

        <div class="p-4 border border-gray-200 rounded-lg bg-white">
          <div class="text-sm text-gray-700 mb-3">Результат</div>
          <div v-if="resultUrl" class="space-y-3">
            <img :src="resultUrl" alt="generated" class="w-full rounded-lg border" />
            <a
              :href="resultUrl"
              target="_blank"
              rel="noreferrer"
              class="text-violet-700 hover:underline text-sm"
            >
              Открыть в новом окне
            </a>
          </div>
          <div v-else class="text-gray-600 text-sm">Ещё нет результата. Запустите генерацию.</div>
        </div>

        <div class="p-4 border border-amber-200 bg-amber-50 text-amber-900 rounded-lg text-sm leading-relaxed">
          Процесс: создаём задачу на генерацию, затем опрашиваем её статус до `DONE` или `FAILED`, после чего показываем ссылку на изображение из хранилища.
        </div>
      </div>
    </div>
  </div>
</template>
