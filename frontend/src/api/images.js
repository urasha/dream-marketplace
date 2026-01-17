import { httpClient } from './httpClient'
import { imageGenConfig } from '../config/imageGen'

const mockResponse = (id) => ({
  id,
  status: 'DONE',
  resultUrl: imageGenConfig.mockUrl,
})

export async function createImageGeneration(payload) {
  if ((imageGenConfig.mode || 'mock') === 'mock') {
    const id = crypto.randomUUID ? crypto.randomUUID() : String(Date.now())
    return mockResponse(id)
  }
  return httpClient.post('/api/images/generate', payload)
}

export async function fetchImageGeneration(id) {
  if ((imageGenConfig.mode || 'mock') === 'mock') {
    return mockResponse(id)
  }
  return httpClient.get(`/api/images/${id}`)
}
