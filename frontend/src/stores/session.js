import { reactive, computed } from 'vue'
import { fetchProfile, updateProfile as apiUpdateProfile } from '../api/profile'

const state = reactive({
  profile: null,
  loading: false,
  error: null,
})

async function loadProfile() {
  state.loading = true
  state.error = null
  try {
    state.profile = await fetchProfile()
  } catch (err) {
    state.error = err
    state.profile = null
  } finally {
    state.loading = false
  }
}

async function updateProfile(payload) {
  state.loading = true
  state.error = null
  try {
    state.profile = await apiUpdateProfile(payload)
    return state.profile
  } catch (err) {
    state.error = err
    throw err
  } finally {
    state.loading = false
  }
}

const isAuthenticated = computed(() => Boolean(state.profile))
const role = computed(() => state.profile?.role?.toLowerCase?.() || 'user')

export function useSessionStore() {
  return {
    state,
    loadProfile,
    updateProfile,
    isAuthenticated,
    role,
  }
}
