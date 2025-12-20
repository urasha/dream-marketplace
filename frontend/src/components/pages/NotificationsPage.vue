<script setup>
import { Bell } from 'lucide-vue-next'
import { notifications } from '../../data/mockData'

const emit = defineEmits(['navigate'])
</script>

<template>
  <div class="max-w-[800px] mx-auto px-6 py-12">
    <div class="flex items-center gap-3 mb-8">
      <Bell class="w-8 h-8 text-violet-600" />
      <h1>Уведомления</h1>
    </div>

    <div class="space-y-4">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="p-6 rounded-xl border transition-all"
        :class="notification.read ? 'bg-white border-gray-200' : 'bg-violet-50 border-violet-200 shadow-sm'"
      >
        <div class="flex items-start justify-between mb-3">
          <div :class="notification.read ? 'text-gray-800' : 'text-gray-900'" class="flex-1">
            {{ notification.message }}
          </div>
          <span v-if="!notification.read" class="ml-3 w-2 h-2 bg-violet-600 rounded-full flex-shrink-0 mt-2" />
        </div>

        <div class="text-gray-600 mb-3">{{ notification.date }}</div>

        <button
          v-if="notification.type === 'generation_ready'"
          @click="emit('navigate', 'dream-detail', 1)"
          class="px-6 py-2 bg-violet-600 text-white rounded-lg hover:bg-violet-700 transition-colors"
        >
          Посмотреть
        </button>

        <button
          v-else-if="notification.type === 'purchase'"
          @click="emit('navigate', 'profile')"
          class="px-6 py-2 bg-violet-600 text-white rounded-lg hover:bg-violet-700 transition-colors"
        >
          Перейти в профиль
        </button>
      </div>
    </div>
  </div>
</template>
