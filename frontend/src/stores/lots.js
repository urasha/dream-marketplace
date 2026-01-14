import { reactive } from 'vue'
import { createLot as apiCreateLot, fetchLots, fetchMyLots, fetchLot } from '../api/lots'

const state = reactive({
  catalog: [],
  mine: [],
  current: null,
  loading: false,
  error: null,
})

async function loadCatalog() {
  state.loading = true
  state.error = null
  try {
    state.catalog = await fetchLots()
  } catch (err) {
    state.error = err
    state.catalog = []
  } finally {
    state.loading = false
  }
}

async function loadMyLots() {
  state.loading = true
  state.error = null
  try {
    state.mine = await fetchMyLots()
  } catch (err) {
    state.error = err
    state.mine = []
  } finally {
    state.loading = false
  }
}

async function loadLot(id) {
  state.loading = true
  state.error = null
  try {
    state.current = await fetchLot(id)
    return state.current
  } catch (err) {
    state.error = err
    state.current = null
    throw err
  } finally {
    state.loading = false
  }
}

async function createLot(payload) {
  const lot = await apiCreateLot(payload)
  state.mine = [lot, ...state.mine]
  state.catalog = [lot, ...state.catalog]
  return lot
}

export function useLotsStore() {
  return {
    state,
    loadCatalog,
    loadMyLots,
    loadLot,
    createLot,
  }
}
