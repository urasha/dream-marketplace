<script setup>
import { computed, onMounted, onUnmounted, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import LotCard from '../cards/LotCard.vue'
import { useLotsStore } from '../../stores/lots'
import { searchTags } from '../../api/tags'

const router = useRouter()
const lotsStore = useLotsStore()

const filters = reactive({
  category: '',
  author: '',
  sort: 'newest',
  tagInput: '',
  selectedTags: [],
  suggestions: [],
  suggestionsLoading: false,
})

const loading = computed(() => lotsStore.state.loading)
const categoryOptions = computed(() => {
  const seen = new Set()
  const options = []
  lotsStore.state.catalog.forEach((lot) => {
    if (lot.categoryName && !seen.has(lot.categoryName)) {
      seen.add(lot.categoryName)
      options.push({ id: lot.categoryId, name: lot.categoryName })
    }
  })
  return options
})

const displayedLots = computed(() => {
  let list = [...lotsStore.state.catalog]

  const parseDate = (value) => {
    const ts = value ? new Date(value).getTime() : 0
    return Number.isFinite(ts) ? ts : 0
  }

  if (filters.category) {
    list = list.filter((lot) => lot.categoryName === filters.category)
  }

  if (filters.author.trim()) {
    const term = filters.author.trim().toLowerCase()
    list = list.filter((lot) => lot.authorName?.toLowerCase().includes(term))
  }

  if (filters.selectedTags.length) {
    const tagNames = filters.selectedTags.map((t) => t.name.toLowerCase())
    list = list.filter((lot) => {
      const lotTags = (lot.tags || []).map((t) => t.toLowerCase())
      return tagNames.every((tag) => lotTags.includes(tag))
    })
  }

  if (filters.sort === 'newest') {
    list = list.sort((a, b) => parseDate(b.submittedAt || b.reviewedAt) - parseDate(a.submittedAt || a.reviewedAt))
  }
  if (filters.sort === 'price-asc') {
    list = list.sort((a, b) => Number(a.price) - Number(b.price))
  }
  if (filters.sort === 'price-desc') {
    list = list.sort((a, b) => Number(b.price) - Number(a.price))
  }

  return list
})

let tagSearchTimer = null
watch(
  () => filters.tagInput,
  (value) => {
    if (tagSearchTimer) {
      clearTimeout(tagSearchTimer)
    }
    if (!value || !value.trim()) {
      filters.suggestions = []
      filters.suggestionsLoading = false
      return
    }

    tagSearchTimer = setTimeout(async () => {
      filters.suggestionsLoading = true
      try {
        const result = await searchTags(value.trim(), 8)
        filters.suggestions = result
      } catch (e) {
        filters.suggestions = []
      } finally {
        filters.suggestionsLoading = false
      }
    }, 200)
  }
)

function addTag(tag) {
  const exists = filters.selectedTags.some(
    (selected) => selected.id === tag.id || selected.name.toLowerCase() === tag.name.toLowerCase()
  )
  if (!exists) {
    filters.selectedTags.push(tag)
  }
  filters.tagInput = ''
  filters.suggestions = []
}

function removeTag(tagName) {
  filters.selectedTags = filters.selectedTags.filter((tag) => tag.name !== tagName)
}

function resetFilters() {
  filters.category = ''
  filters.author = ''
  filters.sort = 'newest'
  filters.tagInput = ''
  filters.selectedTags = []
  filters.suggestions = []
  filters.suggestionsLoading = false
}

onMounted(() => {
  lotsStore.loadCatalog()
})

onUnmounted(() => {
  if (tagSearchTimer) {
    clearTimeout(tagSearchTimer)
  }
})
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <h1 class="mb-8 page-title">Маркетплейс снов</h1>

    <div class="bg-white border border-[#7c3aed]/40 rounded-xl px-4 py-3 mb-8 shadow-sm">
      <div class="flex flex-wrap items-center gap-3 text-sm text-gray-700">
        <div class="flex items-center gap-2">
          <span class="text-xs uppercase tracking-wide text-gray-500">Категория</span>
          <select v-model="filters.category" class="border rounded-md px-2 py-1 text-sm min-w-[140px]">
            <option value="">Все</option>
            <option v-for="cat in categoryOptions" :key="cat.id || cat.name" :value="cat.name">
              {{ cat.name }}
            </option>
          </select>
        </div>

        <div class="flex items-center gap-2">
          <span class="text-xs uppercase tracking-wide text-gray-500">Автор</span>
          <input
            v-model="filters.author"
            type="text"
            placeholder="Имя"
            class="border rounded-md px-2 py-1 text-sm min-w-[140px]"
          />
        </div>

        <div class="flex items-center gap-2">
          <span class="text-xs uppercase tracking-wide text-gray-500">Сортировка</span>
          <select v-model="filters.sort" class="border rounded-md px-2 py-1 text-sm">
            <option value="newest">Новые</option>
            <option value="price-asc">Цена ↑</option>
            <option value="price-desc">Цена ↓</option>
          </select>
        </div>

        <div class="relative flex-1 min-w-[220px]">
          <input
            v-model="filters.tagInput"
            type="text"
            placeholder="Теги"
            class="border rounded-md px-3 py-1.5 w-full text-sm"
          />
          <div v-if="filters.tagInput && (filters.suggestionsLoading || filters.suggestions.length)" class="absolute z-10 mt-1 w-full bg-white border rounded-lg shadow">
            <div v-if="filters.suggestionsLoading" class="px-3 py-2 text-gray-500 text-sm">Ищем теги...</div>
            <button
              v-for="tag in filters.suggestions"
              :key="tag.id || tag.name"
              type="button"
              class="w-full text-left px-3 py-2 hover:bg-gray-50 text-sm"
              @click="addTag(tag)"
            >
              {{ tag.name }}
            </button>
            <div v-if="!filters.suggestionsLoading && !filters.suggestions.length" class="px-3 py-2 text-gray-500 text-sm">
              Ничего не найдено
            </div>
          </div>
        </div>

        <div class="ml-auto flex items-center gap-3">
          <button type="button" class="text-blue-600 text-xs" @click="resetFilters">Сбросить</button>
        </div>
      </div>

      <div v-if="filters.selectedTags.length" class="flex flex-wrap gap-2 mt-3">
        <span
          v-for="tag in filters.selectedTags"
          :key="tag.id || tag.name"
          class="px-3 py-1 bg-[#7c3aed]/10 text-[#5b21b6] border border-[#7c3aed]/30 rounded-full text-sm flex items-center gap-2"
        >
          {{ tag.name }}
          <button type="button" class="text-blue-500" @click="removeTag(tag.name)">×</button>
        </span>
      </div>
    </div>

    <div v-if="loading" class="text-gray-600">Загружаем лоты...</div>
    <div v-else-if="!displayedLots.length" class="text-gray-600">Лотов пока нет</div>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8 items-start">
      <LotCard
        v-for="lot in displayedLots"
        :key="lot.id"
        :title="lot.title"
        :description="lot.description"
        :price="lot.price"
        :author="lot.authorName"
        :author-id="lot.authorId"
        :category="lot.categoryName"
        :image-url="lot.visualizationUrl"
        :rating-average="lot.ratingAverage"
        :rating-count="lot.ratingCount"
        :tags="lot.tags || []"
        @click="() => router.push({ name: 'lot-detail', params: { id: lot.id } })"
        @author-click="(authorId) => router.push({ name: 'author-profile', params: { id: authorId } })"
      />
    </div>
  </div>
</template>
