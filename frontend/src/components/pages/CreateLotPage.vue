<script setup>
import { ref, computed, reactive, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, CheckCircle, AlertCircle } from 'lucide-vue-next'
import { useLotsStore } from '../../stores/lots'
import { useDreamsStore } from '../../stores/dreams'
import { searchTags, fetchTagsByIds } from '../../api/tags'
import { fetchCategories } from '../../api/categories'
import { showError, normalizeErrorMessage } from '../../ui/feedback'

const props = defineProps({
  dreamId: { type: Number, default: null },
  visualizationId: { type: Number, default: null },
})

const router = useRouter()
const lotsStore = useLotsStore()
const dreamsStore = useDreamsStore()

const title = ref('')
const description = ref('')
const price = ref('')
const status = ref('form')
const errorMessage = ref('')
const createdLotId = ref(null)
const categories = ref([])
const selectedCategoryId = ref(null)

const tagsState = reactive({
  input: '',
  suggestions: [],
  suggestionsLoading: false,
  selected: [],
})

const hasContext = computed(() => Boolean(props.dreamId && props.visualizationId))

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push({ name: 'home' })
  }
}

let tagSearchTimer = null

onMounted(async () => {
  await loadCategories()
  await prefillFromDream()
})

onUnmounted(() => {
  if (tagSearchTimer) {
    clearTimeout(tagSearchTimer)
  }
})

watch(
  () => tagsState.input,
  (value) => {
    if (tagSearchTimer) {
      clearTimeout(tagSearchTimer)
    }
    if (!value || !value.trim()) {
      tagsState.suggestions = []
      tagsState.suggestionsLoading = false
      return
    }

    tagSearchTimer = setTimeout(async () => {
      tagsState.suggestionsLoading = true
      try {
        tagsState.suggestions = await searchTags(value.trim(), 8)
      } catch (e) {
        tagsState.suggestions = []
      } finally {
        tagsState.suggestionsLoading = false
      }
    }, 200)
  }
)

const loadCategories = async () => {
  try {
    categories.value = await fetchCategories()
    if (categories.value.length && !selectedCategoryId.value) {
      selectedCategoryId.value = categories.value[0].id
    }
  } catch (e) {
    categories.value = []
  }
}

const prefillFromDream = async () => {
  if (!props.dreamId) return
  try {
    if (!dreamsStore.state.items.length) {
      await dreamsStore.loadDreams()
    }
    const dream = dreamsStore.state.items.find((d) => d.id === props.dreamId)
    if (!dream) return

    if (!title.value) {
      title.value = dream.title || ''
    }
    if (!description.value) {
      description.value = dream.content || ''
    }
    if (dream.categoryId) {
      selectedCategoryId.value = dream.categoryId
    }

    if (!tagsState.selected.length && Array.isArray(dream.tagIds) && dream.tagIds.length) {
      const tags = await fetchTagsByIds(dream.tagIds)
      tagsState.selected = tags.map((t) => ({ id: t.id, name: t.name }))
    }
  } catch (e) {
    // ignore prefill errors
  }
}

const addTag = (tag) => {
  if (!tag || !tag.name) return
  const exists = tagsState.selected.some(
    (t) => t.id === tag.id || t.name.toLowerCase() === tag.name.toLowerCase()
  )
  if (!exists) {
    tagsState.selected.push({ id: tag.id || null, name: tag.name })
  }
  tagsState.input = ''
  tagsState.suggestions = []
  tagsState.suggestionsLoading = false
}

const addTagFromInput = () => {
  const raw = tagsState.input
  if (!raw || !raw.trim()) return
  const names = raw
    .split(/[,;]/)
    .map((n) => n.trim())
    .filter(Boolean)
  names.forEach((name) => addTag({ name }))
}

const removeTag = (name) => {
  tagsState.selected = tagsState.selected.filter((t) => t.name !== name)
}

const handlePublish = async () => {
  if (!hasContext.value) {
    status.value = 'error'
    errorMessage.value = 'Нет данных о выбранной визуализации'
    showError(errorMessage.value)
    return
  }
  if (!title.value || !price.value) {
    status.value = 'error'
    errorMessage.value = 'Укажите название и цену'
    showError(errorMessage.value)
    return
  }

  // захватываем то, что пользователь успел ввести, даже если не нажал "Добавить"
  addTagFromInput()

  status.value = 'loading'
  errorMessage.value = ''
  try {
    const payload = {
      visualizationId: props.visualizationId,
      title: title.value,
      description: description.value || null,
      price: Number(price.value),
      categoryId: selectedCategoryId.value,
      tagIds: tagsState.selected.filter((t) => t.id).map((t) => t.id),
      tagNames: tagsState.selected.filter((t) => !t.id).map((t) => t.name),
    }
    const lot = await lotsStore.createLot(payload)
    createdLotId.value = lot.id
    status.value = 'success'
  } catch (err) {
    status.value = 'error'
    errorMessage.value = err?.userMessage || normalizeErrorMessage(err, 'Не удалось создать лот')
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

    <h1 class="mb-8 page-title">Создать лот</h1>

    <div v-if="status === 'success'" class="p-12 text-center border-2 border-green-600 bg-green-50">
      <CheckCircle class="w-16 h-16 mx-auto mb-4" />
      <h2 class="mb-4">Лот отправлен на модерацию</h2>
      <p class="text-gray-700 mb-6">После проверки модератором лот появится в каталоге.</p>
      <div class="flex gap-4 justify-center">
        <button
          v-if="createdLotId"
          @click="router.push({ name: 'lot-detail', params: { id: createdLotId } })"
          class="px-8 py-3 bg-violet-600 text-white rounded-lg hover:bg-violet-700 transition-colors shadow-sm"
        >
          Открыть лот
        </button>
        <button
          @click="router.push({ name: 'profile' })"
          class="px-8 py-3 border border-violet-200 text-violet-700 rounded-lg hover:border-violet-400 hover:bg-violet-50 transition-colors"
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

        <div>
          <div class="flex items-center justify-between mb-2">
            <label class="block">Категория</label>
            <button type="button" class="text-sm text-violet-600" @click="loadCategories">Обновить</button>
          </div>
          <div class="grid grid-cols-2 sm:grid-cols-3 gap-2">
            <label
              v-for="cat in categories"
              :key="cat.id"
              class="flex items-center gap-2 px-3 py-2 border rounded-lg cursor-pointer transition-colors"
              :class="selectedCategoryId === cat.id
                ? 'bg-violet-600 text-white border-violet-600'
                : 'bg-violet-50 text-violet-700 border-violet-200 hover:border-violet-400'"
            >
              <input
                type="radio"
                :value="cat.id"
                v-model="selectedCategoryId"
                class="text-white focus:ring-white"
              />
              <span class="text-sm">{{ cat.name }}</span>
            </label>
          </div>
        </div>

        <div>
          <div class="flex items-center justify-between mb-2">
            <label class="block">Теги</label>
            <button type="button" class="text-sm text-violet-600" @click="addTagFromInput">Добавить</button>
          </div>
          <div class="relative">
            <input
              v-model="tagsState.input"
              type="text"
              placeholder="Начните вводить тег"
              class="w-full px-4 py-3 border-2 border-gray-300 focus:border-black outline-none"
                @keyup.enter.prevent="addTagFromInput"
                @blur="addTagFromInput"
            />
            <div
              v-if="tagsState.input && (tagsState.suggestionsLoading || tagsState.suggestions.length)"
              class="absolute z-10 mt-1 w-full bg-white border rounded-lg shadow"
            >
              <div v-if="tagsState.suggestionsLoading" class="px-3 py-2 text-gray-500 text-sm">Ищем теги...</div>
              <button
                v-for="tag in tagsState.suggestions"
                :key="tag.id || tag.name"
                type="button"
                class="w-full text-left px-3 py-2 hover:bg-gray-50 text-sm"
                @click="addTag(tag)"
              >
                {{ tag.name }}
              </button>
              <div v-if="!tagsState.suggestionsLoading && !tagsState.suggestions.length" class="px-3 py-2 text-gray-500 text-sm">
                Ничего не найдено
              </div>
            </div>
          </div>
          <div v-if="tagsState.selected.length" class="flex flex-wrap gap-2 mt-3">
            <span
              v-for="tag in tagsState.selected"
              :key="tag.id || tag.name"
              class="px-3 py-1 rounded-full text-sm flex items-center gap-2 border border-amber-200 bg-gradient-to-r from-amber-100 via-rose-100 to-pink-100 text-amber-800"
            >
              {{ tag.name }}
              <button type="button" class="text-amber-600" @click="removeTag(tag.name)">×</button>
            </span>
          </div>
        </div>

        <div class="pt-6 border-t-2 border-gray-300 flex gap-4">
          <button
            @click="handlePublish"
            :disabled="status === 'loading'"
            class="px-8 py-3 bg-violet-600 text-white rounded-lg hover:bg-violet-700 transition-colors shadow-sm disabled:opacity-60"
          >
            {{ status === 'loading' ? 'Сохраняем...' : 'Опубликовать лот' }}
          </button>
          <button
            @click="goBack"
            class="px-8 py-3 border border-violet-200 text-violet-700 rounded-lg hover:border-violet-400 hover:bg-violet-50 transition-colors"
          >
            Отмена
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
