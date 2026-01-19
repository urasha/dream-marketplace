<script setup>
import { ref, reactive, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, AlertCircle, CheckCircle } from 'lucide-vue-next'
import { useDreamsStore } from '../../stores/dreams'
import { searchTags } from '../../api/tags'
import { fetchCategories } from '../../api/categories'

const router = useRouter()

const title = ref('')
const content = ref('')
const isPrivate = ref(false)
const status = ref('idle')
const errorMessage = ref('')
const categories = ref([])
const selectedCategoryId = ref(null)

const tagsState = reactive({
  input: '',
  suggestions: [],
  suggestionsLoading: false,
  selected: [],
})

let tagSearchTimer = null

const dreamsStore = useDreamsStore()

onMounted(async () => {
  await loadCategories()
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

const handleSubmit = async () => {
  errorMessage.value = ''
  if (!title.value || !content.value) {
    status.value = 'error'
    errorMessage.value = 'Заполните название и описание'
    return
  }

  status.value = 'saving'
  try {
    addTagFromInput()
    const payload = {
      title: title.value,
      content: content.value,
      privacy: isPrivate.value ? 'PRIVATE' : 'PUBLIC',
      categoryId: selectedCategoryId.value,
      tagIds: tagsState.selected.filter((t) => t.id).map((t) => t.id),
      tagNames: tagsState.selected.filter((t) => !t.id).map((t) => t.name),
    }
    const created = await dreamsStore.createDream(payload)
    status.value = 'success'
    router.push({ name: 'dream-detail', params: { id: created.id } })
  } catch (err) {
    status.value = 'error'
    errorMessage.value = err?.data?.message || 'Не удалось создать сон'
  }
}
</script>

<template>
  <div class="max-w-[800px] mx-auto px-6 py-12">
    <button
        @click="router.push({ name: 'profile' })"
      class="flex items-center gap-2 mb-6 text-gray-600 hover:text-violet-600 transition-colors"
    >
      <ArrowLeft class="w-5 h-5" />
      Назад к профилю
    </button>

    <h1 class="mb-8 page-title">Создать запись сна</h1>

    <div v-if="status === 'error'" class="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3">
      <AlertCircle class="w-6 h-6 flex-shrink-0 text-red-600" />
      <div>
        <h3 class="text-red-900">Ошибка валидации</h3>
        <p class="text-red-700">{{ errorMessage || 'Пожалуйста, заполните название и описание сна' }}</p>
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
        <div class="flex items-center justify-between mb-2">
          <label class="block">Категория</label>
          <button type="button" class="text-sm text-violet-600" @click="loadCategories">Обновить</button>
        </div>
        <div class="grid grid-cols-2 sm:grid-cols-3 gap-2">
          <label
            v-for="cat in categories"
            :key="cat.id"
            class="flex items-center gap-2 px-3 py-2 border rounded-lg cursor-pointer hover:border-violet-400"
          >
            <input
              type="radio"
              :value="cat.id"
              v-model="selectedCategoryId"
              class="text-violet-600 focus:ring-violet-500"
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
            placeholder="Введите теги или начните ввод"
            class="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent"
            @keyup.enter.prevent="addTagFromInput"
            @blur="addTagFromInput"
          />
          <div
            v-if="tagsState.suggestions.length"
            class="absolute z-10 mt-2 w-full bg-white border border-gray-200 rounded-lg shadow-lg max-h-48 overflow-auto"
          >
            <button
              v-for="tag in tagsState.suggestions"
              :key="tag.id"
              class="w-full text-left px-4 py-2 hover:bg-violet-50"
              @click="addTag(tag)"
            >
              {{ tag.name }}
            </button>
          </div>
        </div>

        <div class="flex flex-wrap gap-2 mt-3">
          <button
            v-for="tag in tagsState.selected"
            :key="tag.id || tag.name"
            type="button"
            class="px-3 py-1 bg-violet-50 text-violet-700 rounded-full text-sm"
            @click="removeTag(tag.name)"
          >
            {{ tag.name }} ×
          </button>
        </div>
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
          :disabled="status === 'saving'"
        >
          {{ status === 'saving' ? 'Сохраняем...' : 'Сохранить запись' }}
        </button>
        <button
          @click="router.push({ name: 'profile' })"
          class="px-8 py-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
        >
          Отмена
        </button>
      </div>
    </div>
  </div>
</template>
