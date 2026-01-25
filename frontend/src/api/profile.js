import { httpClient } from './httpClient'

export async function fetchProfile() {
  return httpClient.get('/api/profile/me')
}

export async function updateProfile(payload) {
  return httpClient.patch('/api/profile/me', payload)
}

export async function uploadAvatar(file) {
  const form = new FormData()
  form.append('file', file)
  return httpClient.post('/api/profile/me/avatar', form)
}
