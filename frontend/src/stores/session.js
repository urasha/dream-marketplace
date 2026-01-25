import { reactive, computed } from 'vue'
import { fetchProfile, updateProfile as apiUpdateProfile, uploadAvatar as apiUploadAvatar } from '../api/profile'
import { logout as apiLogout } from '../api/auth'

const state = reactive({
  profile: null,
  loading: false,
  error: null,
})

function clearToken() {
  try {
    localStorage.removeItem('access_token')
  } catch (e) {
    // ignore storage errors
  }
}

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

async function updateAvatar(file) {
  state.loading = true
  state.error = null
  try {
    state.profile = await apiUploadAvatar(file)
    return state.profile
  } catch (err) {
    state.error = err
    throw err
  } finally {
    state.loading = false
  }
}

async function logout() {
  state.loading = true
  state.error = null
  try {
    await apiLogout()
  } catch (err) {
    state.error = err
    // continue clearing client state even if backend fails
  } finally {
    clearToken()
    state.profile = null
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
    updateAvatar,
    logout,
    isAuthenticated,
    role,
  }
}
