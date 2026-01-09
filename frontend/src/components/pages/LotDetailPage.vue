<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Star, Eye, Download } from 'lucide-vue-next'
import { lots, comments } from '../../data/mockData'

const props = defineProps({
  lotId: { type: Number, default: null },
})

const router = useRouter()

const isPurchased = ref(false)
const lot = computed(() => lots.find((item) => item.id === props.lotId))
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <button
      @click="router.push({ name: 'home' })"
      class="flex items-center gap-2 mb-6 text-gray-600 hover:text-black transition-colors"
    >
      <ArrowLeft class="w-5 h-5" />
      Назад к каталогу
    </button>

    <template v-if="lot">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 mb-12">
        <div>
          <div class="w-full aspect-[4/3] bg-gray-200 border-2 border-gray-300 flex items-center justify-center mb-4">
            <div class="text-gray-400">800×600</div>
          </div>
          <div class="grid grid-cols-4 gap-3">
            <button
              v-for="thumb in 4"
              :key="thumb"
              class="aspect-square bg-gray-200 border-2 border-gray-300 hover:border-black transition-colors"
            />
          </div>
        </div>

        <div>
          <h1 class="mb-4">{{ lot.title }}</h1>

          <button class="text-gray-600 hover:text-black transition-colors mb-4">
            Автор: {{ lot.author }}
          </button>

          <div class="flex items-center gap-6 mb-6 pb-6 border-b-2 border-gray-300">
            <div class="flex items-center gap-2">
              <Star class="w-5 h-5 fill-black" />
              <span>{{ lot.rating }}</span>
            </div>
            <div class="flex items-center gap-2 text-gray-600">
              <Eye class="w-5 h-5" />
              <span>{{ lot.views }}</span>
            </div>
          </div>

          <div class="mb-6">{{ lot.price }} ₽</div>

          <button
            v-if="!isPurchased"
            @click="router.push({ name: 'purchase', params: { id: lot.id } })"
            class="w-full py-4 bg-black text-white hover:bg-gray-800 transition-colors mb-6"
          >
            Купить
          </button>

          <div class="p-6 bg-gray-50 border-2 border-gray-300 mb-6">
            <h3 class="mb-3">Описание</h3>
            <p class="text-gray-700 mb-4">{{ lot.description }}</p>

            <h3 class="mb-3">Условия лицензии</h3>
            <p class="text-gray-700">{{ lot.license }}</p>
          </div>

          <div class="flex flex-wrap gap-2">
            <span
              v-for="(tag, index) in lot.tags"
              :key="index"
              class="px-3 py-2 bg-gray-200 border border-gray-400"
            >
              {{ tag }}
            </span>
          </div>
        </div>
      </div>

      <div v-if="isPurchased" class="p-8 bg-green-50 border-2 border-green-600 mb-8">
        <h2 class="mb-4">Вы владеете этим лотом</h2>
        <p class="text-gray-700 mb-6">Транзакция выполнена успешно. Теперь вы можете скачать файл.</p>

        <div class="p-6 bg-white border-2 border-gray-300 mb-4">
          <div class="flex items-center justify-between">
            <div>
              <div class="mb-1">{{ lot.title }}.png</div>
              <div class="text-gray-600">Размер: 4000×3000px, 2.4 MB</div>
            </div>
            <button class="px-6 py-3 bg-black text-white hover:bg-gray-800 transition-colors flex items-center gap-2">
              <Download class="w-5 h-5" />
              Скачать
            </button>
          </div>
        </div>

        <div class="p-4 bg-gray-100 border border-gray-300">
          <h3 class="mb-2">Детали транзакции</h3>
          <div class="grid grid-cols-2 gap-4 text-gray-700">
            <div>
              <div class="text-gray-600">ID транзакции:</div>
              <div>TXN-2025-11-30-{{ lotId }}892</div>
            </div>
            <div>
              <div class="text-gray-600">Дата покупки:</div>
              <div>30 ноября 2025, 14:23</div>
            </div>
            <div>
              <div class="text-gray-600">Сумма:</div>
              <div>{{ lot.price }} ₽</div>
            </div>
            <div>
              <div class="text-gray-600">Лицензия:</div>
              <div>{{ lot.license }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="mb-12 p-6 bg-gray-50 border-2 border-gray-300">
        <h2 class="mb-4">Метаданные</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 text-gray-700">
          <div>
            <div class="text-gray-600 mb-1">Дата публикации</div>
            <div>{{ lot.date }}</div>
          </div>
          <div>
            <div class="text-gray-600 mb-1">Просмотров</div>
            <div>{{ lot.views }}</div>
          </div>
          <div>
            <div class="text-gray-600 mb-1">Категория</div>
            <div>{{ lot.category }}</div>
          </div>
        </div>
      </div>

      <div>
        <h2 class="mb-6">Отзывы и рейтинги</h2>
        <div class="space-y-6">
          <div v-for="comment in comments" :key="comment.id" class="p-6 bg-white border-2 border-gray-300">
            <div class="flex items-start justify-between mb-3">
              <div>
                <div class="mb-1">{{ comment.author }}</div>
                <div class="flex items-center gap-1">
                  <Star
                    v-for="i in 5"
                    :key="i"
                    class="w-4 h-4"
                    :class="i <= comment.rating ? 'fill-black' : 'fill-gray-300'"
                  />
                </div>
              </div>
              <div class="text-gray-600">{{ comment.date }}</div>
            </div>
            <p class="text-gray-700">{{ comment.text }}</p>
          </div>
        </div>
      </div>
    </template>

    <p v-else>Лот не найден</p>
  </div>
</template>
