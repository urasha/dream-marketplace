<script setup>
import { ref } from 'vue'
import { ArrowLeft, AlertCircle, CheckCircle } from 'lucide-vue-next'

const emit = defineEmits(['navigate'])

const title = ref('')
const content = ref('')
const tags = ref('')
const isPrivate = ref(false)
const status = ref('idle')

const handleSubmit = () => {
  if (!title.value || !content.value) {
    status.value = 'error'
    return
  }
  status.value = 'success'
  setTimeout(() => emit('navigate', 'profile'), 2000)
}
</script>

<template>
  <div class="max-w-[800px] mx-auto px-6 py-12">
    <button
      @click="emit('navigate', 'profile')"
      class="flex items-center gap-2 mb-6 text-gray-600 hover:text-violet-600 transition-colors"
    >
      <ArrowLeft class="w-5 h-5" />
      Назад к профилю
    </button>

    <h1 class="mb-8">Создать запись сна</h1>

    <div v-if="status === 'error'" class="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3">
      <AlertCircle class="w-6 h-6 flex-shrink-0 text-red-600" />
      <div>
        <h3 class="text-red-900">Ошибка валидации</h3>
        <p class="text-red-700">Пожалуйста, заполните название и описание сна</p>
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

      <div>
        <label class="block mb-2">Теги</label>
        <input
          v-model="tags"
          type="text"
          placeholder="полёт, город, свобода (через запятую)"
          class="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent"
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
        >
          Сохранить запись
        </button>
        <button
          @click="emit('navigate', 'profile')"
          class="px-8 py-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
        >
          Отмена
        </button>
      </div>
    </div>

    <div class="mt-12 pt-12 border-t border-gray-200">
      <h2 class="mb-6">Примеры записей</h2>

      <div class="space-y-6">
        <div class="p-6 bg-white border border-gray-200 rounded-xl shadow-sm">
          <h3 class="mb-2">Подводное путешествие</h3>
          <p class="text-gray-700">
            Я нырнул в прозрачную воду и увидел целый подводный город. Здания были покрыты кораллами,
            а между ними плавали светящиеся рыбы. Я мог дышать под водой и свободно перемещаться...
          </p>
          <div class="mt-3 flex flex-wrap gap-2">
            <span class="px-2 py-1 bg-cyan-50 text-cyan-700 rounded-md">вода</span>
            <span class="px-2 py-1 bg-cyan-50 text-cyan-700 rounded-md">город</span>
            <span class="px-2 py-1 bg-cyan-50 text-cyan-700 rounded-md">фантастика</span>
          </div>
        </div>

        <div class="p-6 bg-white border border-gray-200 rounded-xl shadow-sm">
          <h3 class="mb-2">Зеркальный лабиринт</h3>
          <p class="text-gray-700">
            Я оказался в комнате, полностью состоящей из зеркал. Каждое отражение показывало другую
            версию меня в разных мирах. Некоторые были счастливы, другие грустны...
          </p>
          <div class="mt-3 flex flex-wrap gap-2">
            <span class="px-2 py-1 bg-purple-50 text-purple-700 rounded-md">зеркала</span>
            <span class="px-2 py-1 bg-purple-50 text-purple-700 rounded-md">лабиринт</span>
            <span class="px-2 py-1 bg-purple-50 text-purple-700 rounded-md">философия</span>
          </div>
        </div>

        <div class="p-6 bg-white border border-gray-200 rounded-xl shadow-sm">
          <h3 class="mb-2">Говорящие деревья</h3>
          <p class="text-gray-700">
            В лесу все деревья могли разговаривать. Они рассказывали истории о прошлом, делились
            мудростью веков. Самое старое дерево знало моё имя и ждало моего прихода...
          </p>
          <div class="mt-3 flex flex-wrap gap-2">
            <span class="px-2 py-1 bg-green-50 text-green-700 rounded-md">лес</span>
            <span class="px-2 py-1 bg-green-50 text-green-700 rounded-md">природа</span>
            <span class="px-2 py-1 bg-green-50 text-green-700 rounded-md">магия</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
