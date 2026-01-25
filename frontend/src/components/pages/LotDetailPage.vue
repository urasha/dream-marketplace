<script setup>
import { computed, onMounted, reactive, ref, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Download } from 'lucide-vue-next'
import { useLotsStore } from '../../stores/lots'
import { useDreamsStore } from '../../stores/dreams'
import { useSessionStore } from '../../stores/session'
import { addLotComment, fetchLotComments, fetchLotRating, setLotRating, downloadLotAsset } from '../../api/lots'
import { API_BASE } from '../../api/httpClient'
import { confirmAction, normalizeErrorMessage, showError } from '../../ui/feedback'

const props = defineProps({
  lotId: { type: Number, default: null },
})

const router = useRouter()
const route = useRoute()
const lotsStore = useLotsStore()
const dreamsStore = useDreamsStore()
const session = useSessionStore()

const isPurchased = ref(false)
const loading = computed(() => lotsStore.state.loading)
const lot = computed(() => lotsStore.state.current)

const comments = ref([])
const commentsLoading = ref(false)
const commentInput = ref('')
const commentError = ref('')
const downloadError = ref('')
const previewSrc = ref('')

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
const deleteError = ref('')

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push({ name: 'home' })
  }
}

const isOwner = computed(() => {
  const userId = session.state.profile?.id
  return Boolean(userId && lot.value && lot.value.authorId === userId)
})

const isClosed = computed(() => Boolean(lot.value && lot.value.status === 'CLOSED'))
const canDeleteLot = computed(() => Boolean(isOwner.value && lot.value && lot.value.status !== 'SOLD'))
const canManageFeedback = computed(() => !isClosed.value)

const isAvailableForPurchase = computed(() => {
  return Boolean(lot.value && lot.value.status === 'OPEN')
})

const canDownload = computed(() => Boolean(lot.value && lot.value.status === 'SOLD' && !isOwner.value))
const resolvedPreviewUrl = computed(() => {
  const url = lot.value?.visualizationUrl
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
    return url
  }
  if (url.startsWith('/')) {
    return `${API_BASE}${url}`
  }
  return url
})

onMounted(() => {
  if (props.lotId) {
    lotsStore.loadLot(props.lotId).catch(() => {})
    loadComments()
    loadRating()
  }
})

watch(
  () => props.lotId,
  (nextId, prevId) => {
    if (!nextId || nextId === prevId) return
    lotsStore.loadLot(nextId).catch(() => {})
    loadComments()
    loadRating()
  }
)

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
  if (isClosed.value) return
  commentError.value = ''
  const text = commentInput.value.trim()
  if (!text) {
    commentError.value = 'Введите комментарий'
    showError(commentError.value)
    return
  }
  try {
    const created = await addLotComment(props.lotId, text)
    comments.value = [created, ...comments.value]
    commentInput.value = ''
  } catch (e) {
    commentError.value = e?.userMessage || normalizeErrorMessage(e, 'Не удалось отправить комментарий')
  }
}

const chooseRating = async (value) => {
  selectedRating.value = value
  ratingError.value = ''
  ratingSuccess.value = ''
  // сразу показываем выбранное значение как текущую локальную оценку
  rating.userValue = value
  await submitRating()
}

const submitRating = async () => {
  if (isClosed.value) return
  if (!props.lotId || rating.saving) return
  if (!selectedRating.value) {
    ratingError.value = 'Выберите количество звёзд'
    showError(ratingError.value)
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
    ratingError.value = e?.userMessage || normalizeErrorMessage(e, 'Нужно авторизоваться, чтобы поставить оценку')
  } finally {
    rating.saving = false
  }
}

const handleDownload = async () => {
  if (!lot.value) return
  downloadError.value = ''
  try {
    await downloadLotAsset(lot.value.id)
  } catch (e) {
    downloadError.value = normalizeErrorMessage(e, 'Не удалось скачать файл')
    showError(downloadError.value)
  }
}

const handleDelete = async () => {
  if (!lot.value || !canDeleteLot.value) return
  deleteError.value = ''
  const confirmed = await confirmAction({
    title: 'Удалить лот?',
    message: 'Лот будет удалён без возможности восстановления.',
    confirmText: 'Удалить',
    cancelText: 'Отмена',
    tone: 'danger',
  })
  if (!confirmed) return
  try {
    await lotsStore.deleteLot(lot.value.id)
    await dreamsStore.loadDreams().catch(() => {})
    goBack()
  } catch (e) {
    deleteError.value = e?.userMessage || normalizeErrorMessage(e, 'Не удалось удалить лот')
  }
}

const openPreview = (url) => {
  if (!url) return
  previewSrc.value = url
}

const closePreview = () => {
  previewSrc.value = ''
}

const formatDate = (iso) => {
  if (!iso) return ''
  const d = new Date(iso)
  return d.toLocaleString()
}

const activeCommentId = computed(() => {
  const raw = route.query.commentId
  const id = raw ? Number(raw) : null
  return Number.isFinite(id) ? id : null
})

const scrollToComment = async (commentId, attempts = 5) => {
  if (!commentId || attempts <= 0) return
  await nextTick()
  const el = document.getElementById(`comment-${commentId}`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    return
  }
  setTimeout(() => scrollToComment(commentId, attempts - 1), 200)
}

watch(
  () => activeCommentId.value,
  async (id) => {
    if (!id) return
    if (!comments.value.length) {
      await loadComments()
    }
    scrollToComment(id)
  }
)

watch(
  () => comments.value,
  (list) => {
    if (!activeCommentId.value || !list?.length) return
    scrollToComment(activeCommentId.value)
  },
  { deep: true }
)
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <button
      @click="goBack"
      class="flex items-center gap-2 mb-6 text-gray-600 hover:text-black transition-colors"
    >
      <ArrowLeft class="w-5 h-5" />
      Назад
    </button>

    <div v-if="loading" class="text-gray-600">Загрузка...</div>
    <template v-else-if="lot">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 mb-12">
        <div>
          <div class="w-full aspect-[4/3] bg-gray-100 border-2 border-gray-300 flex items-center justify-center mb-4 overflow-hidden rounded-lg">
            <img
              v-if="resolvedPreviewUrl"
              :src="resolvedPreviewUrl"
              alt="Визуализация"
              class="w-full h-full object-cover block clickable-image"
              @click="openPreview(resolvedPreviewUrl)"
            />
            <div v-else class="text-gray-400">Визуализация</div>
          </div>
        </div>

        <div>
          <h1 class="mb-4 page-title">{{ lot.title }}</h1>
          <div class="text-gray-600 mb-2">
            Автор:
            <span
              class="ml-1"
              :class="lot.authorId ? 'text-violet-600 cursor-pointer hover:underline' : ''"
              role="button"
              tabindex="0"
              @click="lot.authorId ? router.push({ name: 'author-profile', params: { id: lot.authorId } }) : null"
              @keydown.enter="lot.authorId ? router.push({ name: 'author-profile', params: { id: lot.authorId } }) : null"
            >
              {{ lot.authorName || '—' }}
            </span>
          </div>
          <div v-if="lot.categoryName" class="text-gray-500 mb-4">Категория: {{ lot.categoryName }}</div>
          <div class="mb-6 text-xl font-semibold">{{ lot.price }} ₽</div>

          <div v-if="canManageFeedback" class="mb-6 p-4 border border-gray-200 rounded-lg bg-white">
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

          <div class="p-6 bg-white border border-gray-200 rounded-xl shadow-sm mb-6">
            <h3 class="mb-3 section-title">Описание</h3>
            <p class="text-gray-800 leading-relaxed">{{ lot.description || 'Описание не указано' }}</p>
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

          <div v-if="isOwner" class="mb-4 p-4 bg-yellow-50 border border-yellow-200 text-yellow-800 rounded-lg">
            Это ваш лот — купить его нельзя.
          </div>
          <div v-if="isClosed" class="mb-4 p-4 bg-gray-100 border border-gray-200 text-gray-600 rounded-lg">
            Лот закрыт — рейтинги и комментарии недоступны.
          </div>
          <button
            v-if="canDeleteLot"
            @click="handleDelete"
            class="w-full py-3 mb-3 rounded-lg transition-colors shadow-sm border border-red-200 text-red-600 hover:bg-red-50"
          >
            Удалить лот
          </button>
          <button
            v-if="canDownload"
            @click="handleDownload"
            class="w-full py-4 rounded-lg transition-colors shadow-sm bg-black text-white hover:bg-gray-800"
          >
            Скачать
          </button>
          <button
            v-else-if="isAvailableForPurchase && !isOwner"
            @click="router.push({ name: 'purchase', params: { id: lot.id } })"
            class="w-full py-4 rounded-lg transition-colors shadow-sm bg-violet-600 text-white hover:bg-violet-700"
          >
            Купить
          </button>

          <div v-if="canDownload" class="mt-3 text-sm text-green-700">
            Вы владеете этим лотом — файл доступен для скачивания.
          </div>
          <div v-if="downloadError" class="mt-2 text-sm text-red-600">{{ downloadError }}</div>
          <div v-if="deleteError" class="mt-2 text-sm text-red-600">{{ deleteError }}</div>
        </div>
      </div>

      <div v-if="canManageFeedback" class="mb-10 p-6 border border-gray-200 rounded-lg bg-white">
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
          <span class="text-sm text-gray-500" v-if="rating.saving">Сохраняем...</span>
          <span class="text-sm text-red-600" v-if="ratingError">{{ ratingError }}</span>
          <span class="text-sm text-green-600" v-if="ratingSuccess">{{ ratingSuccess }}</span>
        </div>
      </div>

      <div v-if="canManageFeedback" class="mt-6 mb-10 p-6 border border-gray-200 rounded-lg bg-white">
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
            :id="`comment-${comment.id}`"
            class="p-4 border rounded-lg transition-colors"
            :class="activeCommentId === comment.id ? 'border-violet-400 bg-violet-50' : 'border-gray-200 bg-white'"
          >
            <div class="flex items-center justify-between mb-1 text-sm text-gray-600">
              <span>{{ comment.username || 'Аноним' }}</span>
              <span>{{ formatDate(comment.createdAt) }}</span>
            </div>
            <p class="text-gray-800">{{ comment.content }}</p>
          </div>
        </div>
      </div>

    </template>

    <p v-else>Лот не найден</p>
  </div>

  <Transition name="lightbox">
    <div v-if="previewSrc" class="lightbox-overlay" @click="closePreview">
      <img :src="previewSrc" alt="preview" class="lightbox-image" @click.stop />
    </div>
  </Transition>
</template>

<style>
.lightbox-enter-active,
.lightbox-leave-active {
  transition: opacity 200ms ease;
}

.lightbox-enter-from,
.lightbox-leave-to {
  opacity: 0;
}

.lightbox-enter-to,
.lightbox-leave-from {
  opacity: 1;
}

.lightbox-overlay {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.78);
  backdrop-filter: blur(2px);
}

.lightbox-image {
  max-width: 90vw;
  max-height: 90vh;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.35);
  transition: transform 200ms ease, opacity 200ms ease;
}
</style>
