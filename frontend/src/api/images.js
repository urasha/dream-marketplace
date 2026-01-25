import { httpClient } from './httpClient'
import { imageGenConfig } from '../config/imageGen'

const mockVariants = () => {
  const base = imageGenConfig.mockUrl || ''
  return Array.from({ length: 4 }, (_, i) => `${base}?v=${i + 1}`)
}

const mockResponse = (id) => ({
  id,
  status: 'DONE',
  resultUrl: imageGenConfig.mockUrl,
  resultUrls: mockVariants(),
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
