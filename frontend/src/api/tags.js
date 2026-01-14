import { httpClient } from './httpClient'

export async function searchTags(query, limit = 20) {
  const params = new URLSearchParams()
  if (query) {
    params.append('q', query)
  }
  params.append('limit', String(limit))
  const suffix = params.toString()
  const path = suffix ? `/api/tags?${suffix}` : '/api/tags'
  return httpClient.get(path)
}
