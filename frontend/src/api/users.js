import { httpClient } from './httpClient'

export async function fetchUserProfile(id) {
  return httpClient.get(`/api/users/${id}/profile`)
}

export async function fetchUserLots(id) {
  return httpClient.get(`/api/users/${id}/lots`)
}

export async function fetchUserDreams(id) {
  return httpClient.get(`/api/users/${id}/dreams`)
}

export async function fetchUserPurchases(id) {
  return httpClient.get(`/api/users/${id}/purchases`)
}
