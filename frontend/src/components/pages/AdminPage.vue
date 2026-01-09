<script setup>
import { ref } from 'vue'
import { CheckCircle, XCircle } from 'lucide-vue-next'
import { moderationQueue, moderationLog } from '../../data/mockData'

const activeTab = ref('queue')
const selectedLot = ref(null)
const showRejectModal = ref(false)
const rejectReason = ref('')

const openRejectModal = (lotId) => {
  selectedLot.value = lotId
  showRejectModal.value = true
}

const closeRejectModal = () => {
  showRejectModal.value = false
  rejectReason.value = ''
  selectedLot.value = null
}

const handleApprove = (lotId) => {
  alert(`Лот ${lotId} одобрен`)
  selectedLot.value = null
}

const handleReject = (lotId) => {
  if (!rejectReason.value) {
    alert('Укажите причину отклонения')
    return
  }
  alert(`Лот ${lotId} отклонён. Причина: ${rejectReason.value}`)
  showRejectModal.value = false
  rejectReason.value = ''
  selectedLot.value = null
}
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <h1 class="mb-8">Панель модератора</h1>

    <div class="flex gap-2 mb-8 border-b border-gray-200">
      <button
        @click="activeTab = 'queue'"
        class="px-6 py-3 transition-colors relative"
        :class="activeTab === 'queue' ? 'text-violet-600' : 'text-gray-600 hover:text-gray-900'"
      >
        Очередь модерации ({{ moderationQueue.length }})
        <div v-if="activeTab === 'queue'" class="absolute bottom-0 left-0 right-0 h-0.5 bg-violet-600" />
      </button>
      <button
        @click="activeTab = 'log'"
        class="px-6 py-3 transition-colors relative"
        :class="activeTab === 'log' ? 'text-violet-600' : 'text-gray-600 hover:text-gray-900'"
      >
        История решений
        <div v-if="activeTab === 'log'" class="absolute bottom-0 left-0 right-0 h-0.5 bg-violet-600" />
      </button>
    </div>

    <div v-if="activeTab === 'queue'" class="space-y-6">
      <div v-for="item in moderationQueue" :key="item.id" class="p-6 bg-white border border-gray-200 rounded-xl shadow-sm">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div>
            <div class="w-full aspect-[4/3] bg-gradient-to-br from-violet-100 via-purple-50 to-indigo-100 border border-gray-200 rounded-lg flex items-center justify-center mb-3">
              <div class="text-violet-400">400×300</div>
            </div>
            <div class="text-gray-600">ID: {{ item.id }}</div>
          </div>

          <div class="lg:col-span-2">
            <h2 class="mb-3">{{ item.title }}</h2>

            <div class="grid grid-cols-2 gap-4 mb-4">
              <div>
                <div class="text-gray-600 mb-1">Автор</div>
                <div>{{ item.author }}</div>
              </div>
              <div>
                <div class="text-gray-600 mb-1">Дата подачи</div>
                <div>{{ item.submittedDate }}</div>
              </div>
              <div>
                <div class="text-gray-600 mb-1">Цена</div>
                <div class="text-violet-600">{{ item.price }} ₽</div>
              </div>
              <div>
                <div class="text-gray-600 mb-1">Теги</div>
                <div class="flex flex-wrap gap-2">
                  <span v-for="(tag, index) in item.tags" :key="index" class="px-2 py-1 bg-violet-50 text-violet-700 rounded-md">
                    {{ tag }}
                  </span>
                </div>
              </div>
            </div>

            <div class="flex gap-4 pt-4 border-t border-gray-200">
              <button
                @click="handleApprove(item.id)"
                class="flex items-center gap-2 px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
              >
                <CheckCircle class="w-5 h-5" />
                Одобрить
              </button>
              <button
                @click="openRejectModal(item.id)"
                class="flex items-center gap-2 px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
              >
                <XCircle class="w-5 h-5" />
                Отклонить
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!moderationQueue.length" class="p-12 text-center bg-white border border-gray-200 rounded-xl">
        <p class="text-gray-600">Очередь модерации пуста</p>
      </div>
    </div>

    <div v-else class="space-y-4">
      <div v-for="entry in moderationLog" :key="entry.id" class="p-6 bg-white border border-gray-200 rounded-xl shadow-sm">
        <div class="flex items-start justify-between mb-3">
          <div>
            <h3 class="mb-1">{{ entry.lotTitle }}</h3>
            <div class="text-gray-600">ID лота: {{ entry.lotId }}</div>
          </div>
          <div
            class="px-3 py-1 rounded-full"
            :class="entry.action === 'approved' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'"
          >
            {{ entry.action === 'approved' ? 'Одобрено' : 'Отклонено' }}
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-gray-700">
          <div>
            <div class="text-gray-600 mb-1">Модератор</div>
            <div>{{ entry.moderator }}</div>
          </div>
          <div>
            <div class="text-gray-600 mb-1">Дата</div>
            <div>{{ entry.date }}</div>
          </div>
        </div>

        <div v-if="entry.reason" class="mt-4 p-4 bg-gray-50 border border-gray-200 rounded-lg">
          <div class="text-gray-600 mb-1">Причина</div>
          <div>{{ entry.reason }}</div>
        </div>
      </div>
    </div>

    <div v-if="showRejectModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-6">
      <div class="bg-white rounded-2xl max-w-xl w-full p-8 shadow-2xl">
        <h2 class="mb-6">Отклонить лот</h2>

        <div class="mb-6">
          <label class="block mb-2">Причина отклонения *</label>
          <textarea
            v-model="rejectReason"
            rows="6"
            placeholder="Укажите конкретную причину отклонения лота..."
            class="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent resize-none"
          />
        </div>

        <div class="flex gap-4">
          <button
            @click="selectedLot && handleReject(selectedLot)"
            class="px-8 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
          >
            Отклонить лот
          </button>
          <button
            @click="closeRejectModal"
            class="px-8 py-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Отмена
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
