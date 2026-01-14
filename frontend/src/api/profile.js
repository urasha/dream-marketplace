import { httpClient } from './httpClient'

export async function fetchProfile() {
  return httpClient.get('/api/profile/me')
}

export async function updateProfile(payload) {
  return httpClient.patch('/api/profile/me', payload)
}
