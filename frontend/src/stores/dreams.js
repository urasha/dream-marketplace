import { reactive } from 'vue'
import { fetchMyDreams, createDream as apiCreateDream, requestVisualization, fetchVisualizations } from '../api/dreams'

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

async function requestDreamVisualization(dreamId) {
  const vis = await requestVisualization(dreamId)
  return vis
}

async function loadVisualizations(dreamId) {
  return fetchVisualizations(dreamId)
}

export function useDreamsStore() {
  return {
    state,
    loadDreams,
    createDream,
    requestDreamVisualization,
    loadVisualizations,
  }
}
