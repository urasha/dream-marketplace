import { httpClient } from './httpClient'

export async function fetchPurchases() {
  return httpClient.get('/api/profile/purchases')
}