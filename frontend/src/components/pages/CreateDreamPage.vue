<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, AlertCircle, CheckCircle } from 'lucide-vue-next'
import { useDreamsStore } from '../../stores/dreams'

const router = useRouter()

const title = ref('')
const content = ref('')
const isPrivate = ref(false)
const status = ref('idle')
const errorMessage = ref('')

const dreamsStore = useDreamsStore()

const handleSubmit = async () => {
  errorMessage.value = ''
  if (!title.value || !content.value) {
    status.value = 'error'
    errorMessage.value = 'Заполните название и описание'
    return
  }

  status.value = 'saving'
  try {
    const payload = {
      title: title.value,
      content: content.value,
      privacy: isPrivate.value ? 'PRIVATE' : 'PUBLIC',
      categoryId: null,
      tagIds: [],
    }
    const created = await dreamsStore.createDream(payload)
    status.value = 'success'
    router.push({ name: 'dream-detail', params: { id: created.id } })
  } catch (err) {
    status.value = 'error'
    errorMessage.value = err?.data?.message || 'Не удалось создать сон'
  }
}
</script>

<template>
  <div class="max-w-[800px] mx-auto px-6 py-12">
    <button
        @click="router.push({ name: 'profile' })"
      class="flex items-center gap-2 mb-6 text-gray-600 hover:text-violet-600 transition-colors"
    >
      <ArrowLeft class="w-5 h-5" />
      Назад к профилю
    </button>

    <h1 class="mb-8 page-title">Создать запись сна</h1>

    <div v-if="status === 'error'" class="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3">
      <AlertCircle class="w-6 h-6 flex-shrink-0 text-red-600" />
      <div>
        <h3 class="text-red-900">Ошибка валидации</h3>
        <p class="text-red-700">{{ errorMessage || 'Пожалуйста, заполните название и описание сна' }}</p>
      </div>
    </div>

    <div v-if="status === 'success'" class="mb-6 p-4 bg-green-50 border border-green-200 rounded-xl flex items-start gap-3">
      <CheckCircle class="w-6 h-6 flex-shrink-0 text-green-600" />
      <div>
        <h3 class="text-green-900">Запись сохранена</h3>
        <p class="text-green-700">Ваш сон успешно добавлен в коллекцию</p>
      </div>
    </div>

    <div class="space-y-6">
      <div>
        <label class="block mb-2">Название сна *</label>
        <input
          v-model="title"
          type="text"
          placeholder="Например: Полёт над городом"
          class="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent"
        />
      </div>

      <div>
        <label class="block mb-2">Описание сна *</label>
        <textarea
          v-model="content"
          rows="12"
          placeholder="Опишите ваш сон подробно. Чем детальнее описание, тем лучше получится визуализация..."
          class="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent resize-none"
        />
      </div>

      <div class="flex items-center gap-3">
        <input
          id="private"
          v-model="isPrivate"
          type="checkbox"
          class="w-5 h-5 rounded border-gray-300 text-violet-600 focus:ring-violet-500"
        />
        <label for="private" class="cursor-pointer">Сделать запись приватной</label>
      </div>

      <div class="pt-6 border-t border-gray-200 flex gap-4">
        <button
          @click="handleSubmit"
          class="px-8 py-3 bg-violet-600 text-white rounded-lg hover:bg-violet-700 transition-colors"
          :disabled="status === 'saving'"
        >
          {{ status === 'saving' ? 'Сохраняем...' : 'Сохранить запись' }}
        </button>
        <button
          @click="router.push({ name: 'profile' })"
          class="px-8 py-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
        >
          Отмена
        </button>
      </div>
    </div>
  </div>
</template>
