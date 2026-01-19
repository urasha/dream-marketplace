import { httpClient } from './httpClient'

export async function fetchMyDreams() {
  return httpClient.get('/api/dreams')
}

export async function createDream(payload) {
  return httpClient.post('/api/dreams', payload)
}

export async function requestVisualization(dreamId) {
  return httpClient.post(`/api/dreams/${dreamId}/visualize`)
}

export async function fetchVisualizations(dreamId) {
  return httpClient.get(`/api/dreams/${dreamId}/visualizations`)
}

export async function attachVisualization(dreamId, payload) {
  return httpClient.post(`/api/dreams/${dreamId}/visualizations/attach`, payload)
}

export async function acceptVisualization(visualizationId) {
  return httpClient.post(`/api/visualizations/${visualizationId}/accept`)
}

export async function deleteDream(id) {
  return httpClient.delete(`/api/dreams/${id}`)
}
