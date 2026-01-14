import { httpClient } from './httpClient'

export async function createLot(payload) {
  return httpClient.post('/api/lots', payload)
}

export async function fetchLots() {
  return httpClient.get('/api/lots')
}

export async function fetchMyLots() {
  return httpClient.get('/api/lots/mine')
}

export async function fetchLot(id) {
  return httpClient.get(`/api/lots/${id}`)
}
