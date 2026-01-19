import { httpClient, API_BASE, getAuthToken } from './httpClient'

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

export async function buyLot(id) {
  return httpClient.post(`/api/lots/${id}/buy`)
}

export async function deleteLot(id) {
  return httpClient.delete(`/api/lots/${id}`)
}

export async function downloadLotAsset(id) {
  const token = getAuthToken()
  const response = await fetch(`${API_BASE}/api/lots/${id}/download`, {
    method: 'GET',
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    credentials: 'include',
  })

  if (response.status === 401) {
    window.location.href = `${API_BASE}/oauth/yandex/login`
    return
  }

  if (!response.ok) {
    const error = new Error('Download failed')
    error.status = response.status
    throw error
  }

  const blob = await response.blob()
  const disposition = response.headers.get('content-disposition') || ''
  const filenameMatch = disposition.match(/filename\*?=([^;]+)/i)
  const rawFilename = filenameMatch ? filenameMatch[1].replace(/"/g, '').trim() : ''
  const filename = rawFilename ? decodeURIComponent(rawFilename) : `lot-${id}.png`

  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
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
