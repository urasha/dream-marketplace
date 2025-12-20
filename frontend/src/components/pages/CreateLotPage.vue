<script setup>
import { ref } from 'vue'
import { ArrowLeft, CheckCircle, AlertCircle, Clock } from 'lucide-vue-next'

const emit = defineEmits(['navigate'])

const title = ref('Полёт над ночным городом')
const description = ref('Визуализация ощущения свободного полёта над огнями большого города')
const price = ref('300')
const license = ref('personal')
const status = ref('form')
const rejectionReason = ref('')

const handlePublish = () => {
  const isApproved = Math.random() > 0.5
  if (isApproved) {
    status.value = 'published'
  } else {
    status.value = 'moderation'
    setTimeout(() => {
      status.value = 'rejected'
      rejectionReason.value = 'Изображение не соответствует описанию сна. Пожалуйста, используйте другую визуализацию.'
    }, 3000)
  }
}
</script>

<template>
  <div class="max-w-[800px] mx-auto px-6 py-12">
    <template v-if="status === 'published'">
      <div class="p-12 text-center border-2 border-green-600 bg-green-50">
        <CheckCircle class="w-16 h-16 mx-auto mb-4" />
        <h1 class="mb-4">Лот успешно опубликован</h1>
        <p class="text-gray-700 mb-6">Ваша визуализация прошла модерацию и теперь доступна в маркетплейсе</p>
        <div class="flex gap-4 justify-center">
          <button @click="emit('navigate', 'home')" class="px-8 py-3 bg-black text-white hover:bg-gray-800 transition-colors">
            Перейти в маркетплейс
          </button>
          <button @click="emit('navigate', 'profile')" class="px-8 py-3 border-2 border-gray-400 hover:border-black transition-colors">
            Мои лоты
          </button>
        </div>
      </div>
    </template>

    <template v-else-if="status === 'moderation'">
      <div class="p-12 text-center border-2 border-yellow-600 bg-yellow-50">
        <Clock class="w-16 h-16 mx-auto mb-4 animate-pulse" />
        <h1 class="mb-4">Лот на модерации</h1>
        <p class="text-gray-700">Ваша визуализация отправлена на проверку модератором. Это может занять некоторое время.</p>
      </div>
    </template>

    <template v-else-if="status === 'rejected'">
      <div class="p-12 border-2 border-red-600 bg-red-50">
        <AlertCircle class="w-16 h-16 mx-auto mb-4" />
        <h1 class="mb-4 text-center">Лот отклонён модератором</h1>
        <div class="mb-6 p-4 bg-white border border-red-300">
          <h3 class="mb-2">Причина отклонения:</h3>
          <p class="text-gray-700">{{ rejectionReason }}</p>
        </div>
        <div class="flex gap-4 justify-center">
          <button @click="status = 'form'" class="px-8 py-3 bg-black text-white hover:bg-gray-800 transition-colors">
            Исправить и отправить заново
          </button>
          <button @click="emit('navigate', 'profile')" class="px-8 py-3 border-2 border-gray-400 hover:border-black transition-colors">
            Вернуться к снам
          </button>
        </div>
      </div>
    </template>

    <template v-else>
      <button
        @click="emit('navigate', 'dream-detail', 1)"
        class="flex items-center gap-2 mb-6 text-gray-600 hover:text-black transition-colors"
      >
        <ArrowLeft class="w-5 h-5" />
        Назад к сну
      </button>

      <h1 class="mb-8">Создать лот</h1>

      <div class="mb-8 p-6 bg-gray-50 border-2 border-gray-300">
        <h3 class="mb-4">Выбранная визуализация</h3>
        <div class="w-full aspect-[4/3] bg-gray-200 border-2 border-gray-400 flex items-center justify-center">
          <div class="text-center">
            <div class="text-gray-400 mb-2">400×300</div>
            <div class="text-gray-600">viz_3</div>
          </div>
        </div>
      </div>

      <div class="space-y-6">
        <div>
          <label class="block mb-2">Название лота *</label>
          <input
            v-model="title"
            type="text"
            class="w-full px-4 py-3 border-2 border-gray-300 focus:border-black outline-none"
          />
        </div>

        <div>
          <label class="block mb-2">Краткое описание *</label>
          <textarea
            v-model="description"
            rows="4"
            class="w-full px-4 py-3 border-2 border-gray-300 focus:border-black outline-none resize-none"
          />
        </div>

        <div>
          <label class="block mb-2">Цена (₽) *</label>
          <input
            v-model="price"
            type="number"
            class="w-full px-4 py-3 border-2 border-gray-300 focus:border-black outline-none"
          />
        </div>

        <div>
          <label class="block mb-2">Условия лицензии *</label>
          <div class="space-y-3">
            <label class="flex items-start gap-3 p-4 border-2 border-gray-300 cursor-pointer hover:border-black transition-colors">
              <input
                v-model="license"
                type="radio"
                name="license"
                value="personal"
                class="mt-1"
              />
              <div>
                <div class="mb-1">Личное использование</div>
                <div class="text-gray-600">Только для некоммерческих целей</div>
              </div>
            </label>

            <label class="flex items-start gap-3 p-4 border-2 border-gray-300 cursor-pointer hover:border-black transition-colors">
              <input
                v-model="license"
                type="radio"
                name="license"
                value="commercial"
                class="mt-1"
              />
              <div>
                <div class="mb-1">Коммерческая лицензия</div>
                <div class="text-gray-600">Разрешено использование в коммерческих проектах</div>
              </div>
            </label>

            <label class="flex items-start gap-3 p-4 border-2 border-gray-300 cursor-pointer hover:border-black transition-colors">
              <input
                v-model="license"
                type="radio"
                name="license"
                value="full"
                class="mt-1"
              />
              <div>
                <div class="mb-1">Полная лицензия</div>
                <div class="text-gray-600">Включая право на модификацию и перепродажу</div>
              </div>
            </label>
          </div>
        </div>

        <div class="pt-6 border-t-2 border-gray-300 flex gap-4">
          <button @click="handlePublish" class="px-8 py-3 bg-black text-white hover:bg-gray-800 transition-colors">
            Опубликовать лот
          </button>
          <button
            @click="emit('navigate', 'dream-detail', 1)"
            class="px-8 py-3 border-2 border-gray-400 hover:border-black transition-colors"
          >
            Отмена
          </button>
        </div>
      </div>
    </template>
  </div>
</template>
