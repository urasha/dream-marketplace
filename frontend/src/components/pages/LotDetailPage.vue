<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Download } from 'lucide-vue-next'
import { useLotsStore } from '../../stores/lots'
import { addLotComment, fetchLotComments, fetchLotRating, setLotRating } from '../../api/lots'

const props = defineProps({
  lotId: { type: Number, default: null },
})

const router = useRouter()
const route = useRoute()
const lotsStore = useLotsStore()

const isPurchased = ref(false)
const loading = computed(() => lotsStore.state.loading)
const lot = computed(() => lotsStore.state.current)

const comments = ref([])
const commentsLoading = ref(false)
const commentInput = ref('')
const commentError = ref('')

const rating = reactive({
  average: 0,
  count: 0,
  userValue: null,
  loading: true,
  saving: false,
})
const ratingError = ref('')
const ratingSuccess = ref('')
const selectedRating = ref(null)
const hoverRating = ref(null)

const backTarget = computed(() => {
  if (route.query.from === 'profile-lots') {
    return { name: 'profile', query: { tab: 'lots' } }
  }
  return { name: 'home' }
})

onMounted(() => {
  if (props.lotId) {
    lotsStore.loadLot(props.lotId).catch(() => {})
    loadComments()
    loadRating()
  }
})

const loadComments = async () => {
  if (!props.lotId) return
  commentsLoading.value = true
  try {
    comments.value = await fetchLotComments(props.lotId)
  } catch (e) {
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

const loadRating = async () => {
  if (!props.lotId) return
  rating.loading = true
  try {
    const data = await fetchLotRating(props.lotId)
    rating.average = data.average || 0
    rating.count = data.count || 0
    rating.userValue = data.userValue ?? null
    selectedRating.value = data.userValue ?? null
  } catch (e) {
    rating.average = 0
    rating.count = 0
    rating.userValue = null
    selectedRating.value = null
  } finally {
    rating.loading = false
  }
}

const submitComment = async () => {
  commentError.value = ''
  const text = commentInput.value.trim()
  if (!text) {
    commentError.value = 'Введите комментарий'
    return
  }
  try {
    const created = await addLotComment(props.lotId, text)
    comments.value = [created, ...comments.value]
    commentInput.value = ''
  } catch (e) {
    commentError.value = e?.data?.message || 'Не удалось отправить комментарий'
  }
}

const chooseRating = (value) => {
  selectedRating.value = value
  ratingError.value = ''
  ratingSuccess.value = ''
  // сразу показываем выбранное значение как текущую локальную оценку
  rating.userValue = value
}

const submitRating = async () => {
  if (!props.lotId || rating.saving) return
  if (!selectedRating.value) {
    ratingError.value = 'Выберите количество звёзд'
    return
  }
  ratingError.value = ''
  ratingSuccess.value = ''
  rating.saving = true
  try {
    const summary = await setLotRating(props.lotId, selectedRating.value)
    rating.average = summary.average || 0
    rating.count = summary.count || 0
    rating.userValue = summary.userValue ?? selectedRating.value
    selectedRating.value = rating.userValue
    ratingSuccess.value = 'Оценка сохранена'
  } catch (e) {
    ratingError.value = e?.data?.message || 'Нужно авторизоваться, чтобы поставить оценку'
  } finally {
    rating.saving = false
  }
}

const formatDate = (iso) => {
  if (!iso) return ''
  const d = new Date(iso)
  return d.toLocaleString()
}
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <button
      @click="router.push(backTarget)"
      class="flex items-center gap-2 mb-6 text-gray-600 hover:text-black transition-colors"
    >
      <ArrowLeft class="w-5 h-5" />
      Назад
    </button>

    <div v-if="loading" class="text-gray-600">Загрузка...</div>
    <template v-else-if="lot">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 mb-12">
        <div>
          <div class="w-full aspect-[4/3] bg-gray-100 border-2 border-gray-300 flex items-center justify-center mb-4 overflow-hidden">
            <img
              v-if="lot.visualizationUrl"
              :src="lot.visualizationUrl"
              alt="Визуализация"
              class="w-full h-full object-cover"
            />
            <div v-else class="text-gray-400">Визуализация</div>
          </div>
        </div>

        <div>
          <h1 class="mb-4">{{ lot.title }}</h1>
          <div class="text-gray-600 mb-2">Автор: {{ lot.authorName || '—' }}</div>
          <div v-if="lot.categoryName" class="text-gray-500 mb-4">Категория: {{ lot.categoryName }}</div>
          <div class="mb-6 text-xl font-semibold">{{ lot.price }} ₽</div>

          <div class="mb-6 p-4 border border-gray-200 rounded-lg bg-white">
            <div class="flex items-center justify-between mb-2">
              <div class="font-medium">Рейтинг</div>
              <div class="text-sm text-gray-600" v-if="!rating.loading">{{ rating.count }} оценок</div>
            </div>
            <div class="flex items-center gap-2">
              <span
                v-for="star in 5"
                :key="star"
                class="text-2xl select-none"
                :class="(Math.round(rating.average) || 0) >= star ? 'text-amber-400' : 'text-gray-400'"
              >
                ★
              </span>
              <div class="text-gray-700 text-sm" v-if="!rating.loading">
                {{ rating.average.toFixed(1) }} / 5
              </div>
            </div>
          </div>

          <div class="p-6 bg-gray-50 border-2 border-gray-300 mb-6">
            <h3 class="mb-3">Описание</h3>
            <p class="text-gray-700">{{ lot.description || 'Описание не указано' }}</p>
          </div>

          <div v-if="lot.tags?.length" class="mb-6 flex flex-wrap gap-2">
            <span
              v-for="(tag, idx) in lot.tags"
              :key="idx"
              class="px-3 py-1 bg-violet-50 text-violet-700 rounded-full border border-violet-100 text-sm"
            >
              {{ tag }}
            </span>
          </div>

          <button
            @click="router.push({ name: 'purchase', params: { id: lot.id } })"
            class="w-full py-4 bg-black text-white hover:bg-gray-800 transition-colors"
          >
            Купить
          </button>
        </div>
      </div>

      <div class="mb-10 p-6 border border-gray-200 rounded-lg bg-white">
        <h2 class="text-lg font-semibold mb-3">Ваша оценка</h2>
        <div class="flex items-center gap-2 mb-3">
          <button
            v-for="star in 5"
            :key="star"
            type="button"
            class="text-2xl transition-transform hover:scale-110"
            :class="((hoverRating ?? selectedRating ?? rating.userValue ?? 0) >= star) ? 'text-amber-400' : 'text-gray-400'"
            @click="chooseRating(star)
            "
            @mouseenter="hoverRating = star"
            @mouseleave="hoverRating = null"
            :disabled="rating.saving"
          >
            ★
          </button>
          <span class="text-gray-600 text-sm" v-if="rating.userValue">Текущая: {{ rating.userValue }} ★</span>
        </div>
        <div class="flex items-center gap-3">
          <button
            class="px-4 py-2 bg-black text-white rounded-lg hover:bg-gray-800 disabled:opacity-60"
            :disabled="rating.saving"
            @click="submitRating"
          >
            Подтвердить
          </button>
          <span class="text-sm text-red-600" v-if="ratingError">{{ ratingError }}</span>
          <span class="text-sm text-green-600" v-if="ratingSuccess">{{ ratingSuccess }}</span>
        </div>
      </div>

      <div class="mb-10 p-6 border border-gray-200 rounded-lg bg-white">
        <h2 class="text-lg font-semibold mb-4">Комментарии</h2>

        <div class="mb-4">
          <textarea
            v-model="commentInput"
            rows="3"
            placeholder="Оставьте свой отзыв"
            class="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500"
          ></textarea>
          <div class="flex items-center justify-between mt-2">
            <span class="text-red-600 text-sm" v-if="commentError">{{ commentError }}</span>
            <button
              class="px-4 py-2 bg-violet-600 text-white rounded-lg hover:bg-violet-700"
              @click="submitComment"
            >
              Отправить
            </button>
          </div>
        </div>

        <div v-if="commentsLoading" class="text-gray-600">Загружаем комментарии...</div>
        <div v-else-if="!comments.length" class="text-gray-600">Пока нет комментариев</div>
        <div v-else class="space-y-4">
          <div
            v-for="comment in comments"
            :key="comment.id"
            class="p-4 border border-gray-200 rounded-lg bg-white"
          >
            <div class="flex items-center justify-between mb-1 text-sm text-gray-600">
              <span>{{ comment.username || 'Аноним' }}</span>
              <span>{{ formatDate(comment.createdAt) }}</span>
            </div>
            <p class="text-gray-800">{{ comment.content }}</p>
          </div>
        </div>
      </div>

      <div v-if="isPurchased" class="p-8 bg-green-50 border-2 border-green-600 mb-8">
        <h2 class="mb-4">Вы владеете этим лотом</h2>
        <p class="text-gray-700 mb-6">Теперь вы можете скачать файл.</p>

        <div class="p-6 bg-white border-2 border-gray-300 mb-4">
          <div class="flex items-center justify-between">
            <div>
              <div class="mb-1">{{ lot.title }}.asset</div>
              <div class="text-gray-600">Файл доступен для скачивания</div>
            </div>
            <button class="px-6 py-3 bg-black text-white hover:bg-gray-800 transition-colors flex items-center gap-2">
              <Download class="w-5 h-5" />
              Скачать
            </button>
          </div>
        </div>
      </div>
    </template>

    <p v-else>Лот не найден</p>
  </div>
</template>
