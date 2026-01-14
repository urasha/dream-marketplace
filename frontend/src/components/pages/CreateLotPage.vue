<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, CheckCircle, AlertCircle } from 'lucide-vue-next'
import { useLotsStore } from '../../stores/lots'

const props = defineProps({
  dreamId: { type: Number, default: null },
  visualizationId: { type: Number, default: null },
})

const router = useRouter()
const lotsStore = useLotsStore()

const title = ref('')
const description = ref('')
const price = ref('')
const status = ref('form')
const errorMessage = ref('')
const createdLotId = ref(null)

const hasContext = computed(() => Boolean(props.dreamId && props.visualizationId))

const goBack = () => {
  if (props.dreamId) {
    router.push({ name: 'dream-detail', params: { id: props.dreamId } })
  } else {
    router.push({ name: 'profile' })
  }
}

const handlePublish = async () => {
  if (!hasContext.value) {
    status.value = 'error'
    errorMessage.value = 'Нет данных о выбранной визуализации'
    return
  }
  if (!title.value || !price.value) {
    status.value = 'error'
    errorMessage.value = 'Укажите название и цену'
    return
  }

  status.value = 'loading'
  errorMessage.value = ''
  try {
    const payload = {
      visualizationId: props.visualizationId,
      title: title.value,
      description: description.value || null,
      price: Number(price.value),
    }
    const lot = await lotsStore.createLot(payload)
    createdLotId.value = lot.id
    status.value = 'success'
  } catch (err) {
    status.value = 'error'
    errorMessage.value = err?.data?.message || 'Не удалось создать лот'
  }
}
</script>

<template>
  <div class="max-w-[800px] mx-auto px-6 py-12">
    <button
      @click="goBack"
      class="flex items-center gap-2 mb-6 text-gray-600 hover:text-black transition-colors"
    >
      <ArrowLeft class="w-5 h-5" />
      Назад
    </button>

    <h1 class="mb-8">Создать лот</h1>

    <div v-if="status === 'success'" class="p-12 text-center border-2 border-green-600 bg-green-50">
      <CheckCircle class="w-16 h-16 mx-auto mb-4" />
      <h2 class="mb-4">Лот создан</h2>
      <p class="text-gray-700 mb-6">Ваша визуализация опубликована как лот.</p>
      <div class="flex gap-4 justify-center">
        <button
          v-if="createdLotId"
          @click="router.push({ name: 'lot-detail', params: { id: createdLotId } })"
          class="px-8 py-3 bg-black text-white hover:bg-gray-800 transition-colors"
        >
          Открыть лот
        </button>
        <button
          @click="router.push({ name: 'profile' })"
          class="px-8 py-3 border-2 border-gray-400 hover:border-black transition-colors"
        >
          Профиль
        </button>
      </div>
    </div>

    <div v-else>
      <div v-if="status === 'error'" class="mb-6 p-4 bg-red-50 border-2 border-red-200 flex items-start gap-3">
        <AlertCircle class="w-6 h-6 flex-shrink-0 text-red-600" />
        <div>
          <h3 class="text-red-900">Не удалось создать лот</h3>
          <p class="text-red-700">{{ errorMessage }}</p>
        </div>
      </div>

      <div class="mb-8 p-6 bg-gray-50 border-2 border-gray-300">
        <h3 class="mb-2">Контекст</h3>
        <p class="text-gray-700" v-if="hasContext">Сон #{{ dreamId }} · Визуализация #{{ visualizationId }}</p>
        <p class="text-red-700" v-else>Не выбрана визуализация — вернитесь и выберите готовый вариант.</p>
      </div>

      <div class="space-y-6">
        <div>
          <label class="block mb-2">Название лота *</label>
          <input
            v-model="title"
            type="text"
            placeholder="Например: Полёт над ночным городом"
            class="w-full px-4 py-3 border-2 border-gray-300 focus:border-black outline-none"
          />
        </div>

        <div>
          <label class="block mb-2">Описание</label>
          <textarea
            v-model="description"
            rows="4"
            placeholder="Расскажите, что покупатель получит вместе с визуализацией"
            class="w-full px-4 py-3 border-2 border-gray-300 focus:border-black outline-none resize-none"
          />
        </div>

        <div>
          <label class="block mb-2">Цена (₽) *</label>
          <input
            v-model="price"
            type="number"
            min="1"
            step="0.01"
            class="w-full px-4 py-3 border-2 border-gray-300 focus:border-black outline-none"
          />
        </div>

        <div class="pt-6 border-t-2 border-gray-300 flex gap-4">
          <button
            @click="handlePublish"
            :disabled="status === 'loading'"
            class="px-8 py-3 bg-black text-white hover:bg-gray-800 transition-colors disabled:bg-gray-500"
          >
            {{ status === 'loading' ? 'Сохраняем...' : 'Опубликовать лот' }}
          </button>
          <button
            @click="goBack"
            class="px-8 py-3 border-2 border-gray-400 hover:border-black transition-colors"
          >
            Отмена
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
