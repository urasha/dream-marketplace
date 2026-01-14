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

export async function fetchLotComments(id) {
  return httpClient.get(`/api/lots/${id}/comments`)
}

export async function addLotComment(id, content) {
  return httpClient.post(`/api/lots/${id}/comments`, { content })
}

export async function fetchLotRating(id) {
  return httpClient.get(`/api/lots/${id}/rating`)
}

export async function setLotRating(id, value) {
  return httpClient.put(`/api/lots/${id}/rating`, { value })
}
