<script setup>
import { ref } from 'vue'
import { User, Plus } from 'lucide-vue-next'
import { userLots, userTransactions, dreams } from '../../data/mockData'
import DreamCard from '../cards/DreamCard.vue'

const props = defineProps({
  userBalance: { type: Number, required: true },
})

const emit = defineEmits(['navigate'])

const activeTab = ref('dreams')

const tabs = [
  { id: 'dreams', label: 'Мои сны' },
  { id: 'lots', label: 'Мои лоты' },
  { id: 'purchases', label: 'Мои покупки' },
]
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <h1 class="mb-8">Профиль</h1>

    <div class="mb-8 p-6 bg-white rounded-xl border border-gray-200 shadow-sm">
      <div class="flex items-start gap-6">
        <div class="w-24 h-24 bg-gradient-to-br from-violet-500 to-indigo-500 rounded-xl flex items-center justify-center flex-shrink-0">
          <User class="w-12 h-12 text-white" />
        </div>
        <div class="flex-1">
          <h2 class="mb-2">Иван Петров</h2>
          <div class="text-gray-600 mb-4">ivan.petrov@example.com</div>
          <p class="text-gray-700 mb-4">
            Художник-визуализатор, работаю с образами из сновидений. Создаю уникальные арт-работы на основе подсознательных переживаний.
          </p>
          <div class="flex items-center gap-6">
            <div>
              <div class="text-gray-600">Баланс</div>
              <div class="text-violet-600">{{ userBalance }} ₽</div>
            </div>
            <div>
              <div class="text-gray-600">Лотов создано</div>
              <div>{{ userLots.length }}</div>
            </div>
            <div>
              <div class="text-gray-600">Покупок</div>
              <div>{{ userTransactions.filter((t) => t.type === 'purchase').length }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="mb-6">
      <div class="flex gap-2 border-b border-gray-200">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          @click="activeTab = tab.id"
          class="px-6 py-3 transition-colors relative"
          :class="activeTab === tab.id ? 'text-violet-600' : 'text-gray-600 hover:text-gray-900'"
        >
          {{ tab.label }}
          <div v-if="activeTab === tab.id" class="absolute bottom-0 left-0 right-0 h-0.5 bg-violet-600" />
        </button>
      </div>
    </div>

    <div v-if="activeTab === 'dreams'">
      <div class="flex items-center justify-between mb-6">
        <h3>Мои записи снов</h3>
        <button
          @click="emit('navigate', 'create-dream')"
          class="flex items-center gap-2 px-4 py-2 bg-violet-600 text-white rounded-lg hover:bg-violet-700 transition-colors"
        >
          <Plus class="w-4 h-4" />
          Создать запись
        </button>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <DreamCard
          v-for="dream in dreams"
          :key="dream.id"
          v-bind="dream"
          @click="emit('navigate', 'dream-detail', dream.id)"
        />
      </div>
    </div>

    <div v-else-if="activeTab === 'lots'">
      <h3 class="mb-6">Мои лоты</h3>
      <div class="space-y-4">
        <div
          v-for="lot in userLots"
          :key="lot.id"
          class="p-6 bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-shadow"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <h3 class="mb-3">{{ lot.title }}</h3>
              <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div>
                  <div class="text-gray-600">Цена</div>
                  <div>{{ lot.price }} ₽</div>
                </div>
                <div>
                  <div class="text-gray-600">Статус</div>
                  <span
                    class="inline-block px-3 py-1 rounded-full"
                    :class="lot.status === 'published' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'"
                  >
                    {{ lot.status === 'published' ? 'Опубликован' : 'На модерации' }}
                  </span>
                </div>
                <div v-if="lot.sales > 0">
                  <div class="text-gray-600">Продаж</div>
                  <div>{{ lot.sales }}</div>
                </div>
                <div v-if="lot.sales > 0">
                  <div class="text-gray-600">Доход</div>
                  <div class="text-violet-600">{{ lot.revenue }} ₽</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else>
      <h3 class="mb-6">Мои покупки</h3>
      <div class="space-y-4">
        <div
          v-for="transaction in userTransactions"
          :key="transaction.id"
          class="p-6 bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-shadow"
        >
          <template v-if="transaction.type === 'purchase'">
            <div class="flex items-start justify-between mb-4">
              <div class="flex-1">
                <h3 class="mb-2">{{ transaction.lotTitle }}</h3>
                <div class="text-gray-600">{{ transaction.date }}</div>
              </div>
              <div class="text-right">
                <div class="text-gray-600">Сумма</div>
                <div>{{ transaction.amount }} ₽</div>
              </div>
            </div>
            <button
              @click="emit('navigate', 'lot-detail', transaction.lotId)"
              class="w-full md:w-auto px-6 py-2 bg-violet-600 text-white rounded-lg hover:bg-violet-700 transition-colors"
            >
              Открыть лот
            </button>
          </template>

          <template v-else>
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <h3 class="mb-2">Пополнение баланса</h3>
                <div class="text-gray-600">{{ transaction.date }}</div>
              </div>
              <div class="text-right">
                <div class="text-gray-600">Сумма</div>
                <div class="text-green-600">+{{ transaction.amount }} ₽</div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>
