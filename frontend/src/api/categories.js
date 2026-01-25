import { httpClient } from './httpClient'

export async function fetchCategories() {
  return httpClient.get('/api/categories')
}
