import { reactive } from 'vue'
import { fetchMyDreams, createDream as apiCreateDream, updateDream as apiUpdateDream, requestVisualization, fetchVisualizations, deleteDream as apiDeleteDream } from '../api/dreams'

const state = reactive({
  items: [],
  loading: false,
  error: null,
})

async function loadDreams() {
  state.loading = true
  state.error = null
  try {
    state.items = await fetchMyDreams()
  } catch (err) {
    state.error = err
    state.items = []
  } finally {
    state.loading = false
  }
}

async function createDream(payload) {
  const dream = await apiCreateDream(payload)
  state.items = [dream, ...state.items]
  return dream
}

async function updateDream(id, payload) {
  const updated = await apiUpdateDream(id, payload)
  state.items = state.items.map((dream) => (dream.id === updated.id ? updated : dream))
  return updated
}

async function requestDreamVisualization(dreamId) {
  const vis = await requestVisualization(dreamId)
  return vis
}

async function loadVisualizations(dreamId) {
  return fetchVisualizations(dreamId)
}

async function deleteDream(id) {
  await apiDeleteDream(id)
  state.items = state.items.filter((dream) => dream.id !== id)
}

export function useDreamsStore() {
  return {
    state,
    loadDreams,
    createDream,
    updateDream,
    requestDreamVisualization,
    loadVisualizations,
    deleteDream,
  }
}
