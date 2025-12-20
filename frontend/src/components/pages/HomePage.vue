<script setup>
import { ref, computed } from 'vue'
import { ChevronLeft, ChevronRight, X, Search } from 'lucide-vue-next'
import LotCard from '../cards/LotCard.vue'
import { lots } from '../../data/mockData'

const emit = defineEmits(['navigate'])

const selectedCategory = ref('all')
const selectedTags = ref([])
const sortBy = ref('date')
const currentPage = ref(1)
const tagSearchQuery = ref('')

const categories = ['all', 'сюрреализм', 'романтика', 'абстракт', 'кошмар']
const allTags = ['полёт', 'город', 'вода', 'космос', 'архитектура', 'страх', 'свобода']
const lotsPerPage = 6

const filteredLots = computed(() => {
  return lots
    .filter((lot) => selectedCategory.value === 'all' || lot.category === selectedCategory.value)
    .filter((lot) => selectedTags.value.length === 0 || selectedTags.value.some((tag) => lot.tags.includes(tag)))
    .slice()
    .sort((a, b) => {
      if (sortBy.value === 'date') {
        return new Date(b.date).getTime() - new Date(a.date).getTime()
      }
      return b.price - a.price
    })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredLots.value.length / lotsPerPage)))
const displayedLots = computed(() => {
  const start = (currentPage.value - 1) * lotsPerPage
  return filteredLots.value.slice(start, start + lotsPerPage)
})

const toggleTag = (tag) => {
  if (selectedTags.value.includes(tag)) {
    selectedTags.value = selectedTags.value.filter((t) => t !== tag)
  } else {
    selectedTags.value = [...selectedTags.value, tag]
  }
}

const removeTag = (tag) => {
  selectedTags.value = selectedTags.value.filter((t) => t !== tag)
}

const filteredTagSuggestions = computed(() =>
  allTags.filter(
    (tag) => tag.toLowerCase().includes(tagSearchQuery.value.toLowerCase()) && !selectedTags.value.includes(tag)
  )
)

const goToPage = (page) => {
  const next = Math.min(Math.max(1, page), totalPages.value)
  currentPage.value = next
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <h1 class="mb-8">Маркетплейс снов</h1>

    <div class="mb-8 p-6 bg-white rounded-xl border border-gray-200 shadow-sm">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block mb-2 text-gray-700">Категория</label>
          <select
            v-model="selectedCategory"
            class="w-full px-4 py-2 bg-white border border-gray-200 rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent"
          >
            <option value="all">Все категории</option>
            <option v-for="category in categories.filter((c) => c !== 'all')" :key="category" :value="category">
              {{ category }}
            </option>
          </select>
        </div>

        <div>
          <label class="block mb-2 text-gray-700">Сортировка</label>
          <select
            v-model="sortBy"
            class="w-full px-4 py-2 bg-white border border-gray-200 rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent"
          >
            <option value="date">По дате</option>
            <option value="price">По цене</option>
          </select>
        </div>

        <div>
          <label class="block mb-2 text-gray-700">Теги</label>
          <div class="relative">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 z-10" />
            <input
              v-model="tagSearchQuery"
              type="text"
              placeholder="Поиск по тегам..."
              class="w-full pl-10 pr-4 py-2 bg-white border border-gray-200 rounded-lg text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent"
            />

            <div
              v-if="tagSearchQuery && filteredTagSuggestions.length"
              class="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-20 max-h-48 overflow-y-auto"
            >
              <button
                v-for="tag in filteredTagSuggestions"
                :key="tag"
                @click="() => { toggleTag(tag); tagSearchQuery = '' }"
                class="w-full text-left px-4 py-2 hover:bg-violet-50 transition-colors"
              >
                {{ tag }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="selectedTags.length" class="mt-4 pt-4 border-t border-gray-200">
        <div class="flex flex-wrap gap-2">
          <button
            v-for="tag in selectedTags"
            :key="tag"
            @click="() => removeTag(tag)"
            class="flex items-center gap-2 px-3 py-1 bg-violet-100 text-violet-700 rounded-full hover:bg-violet-200 transition-colors"
          >
            {{ tag }}
            <X class="w-3 h-3" />
          </button>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
      <LotCard
        v-for="lot in displayedLots"
        :key="lot.id"
        v-bind="lot"
        @click="() => emit('navigate', 'lot-detail', lot.id)"
      />
    </div>

    <div class="flex items-center justify-center gap-4" v-if="totalPages > 1">
      <button
        @click="() => goToPage(currentPage - 1)"
        :disabled="currentPage === 1"
        class="p-2 border border-gray-200 rounded-lg hover:bg-violet-50 hover:border-violet-300 disabled:opacity-30 disabled:hover:bg-white disabled:hover:border-gray-200 transition-colors"
      >
        <ChevronLeft class="w-5 h-5" />
      </button>

      <div class="flex gap-2">
        <button
          v-for="page in totalPages"
          :key="page"
          @click="() => goToPage(page)"
          class="px-4 py-2 rounded-lg transition-colors"
          :class="currentPage === page ? 'bg-violet-600 text-white' : 'border border-gray-200 hover:bg-violet-50 hover:border-violet-300'"
        >
          {{ page }}
        </button>
      </div>

      <button
        @click="() => goToPage(currentPage + 1)"
        :disabled="currentPage === totalPages"
        class="p-2 border border-gray-200 rounded-lg hover:bg-violet-50 hover:border-violet-300 disabled:opacity-30 disabled:hover:bg-white disabled:hover:border-gray-200 transition-colors"
      >
        <ChevronRight class="w-5 h-5" />
      </button>
    </div>
  </div>
</template>
