import { httpClient } from './httpClient'

export async function fetchModerationQueue() {
  return httpClient.get('/api/admin/lots/pending')
}

export async function fetchModerationLog() {
  return httpClient.get('/api/admin/moderation-log')
}

export async function approveLot(lotId) {
  return httpClient.post(`/api/admin/lots/${lotId}/approve`)
}

export async function rejectLot(lotId, reason) {
  return httpClient.post(`/api/admin/lots/${lotId}/reject`, { reason })
}