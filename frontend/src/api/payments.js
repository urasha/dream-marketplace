import { httpClient } from './httpClient'

export async function fetchWallet() {
  return httpClient.get('/api/payments/wallet')
}

export async function createDeposit(amount) {
  return httpClient.post('/api/payments/deposit', { amount })
}

export async function fetchPaymentStatus(id) {
  return httpClient.get(`/api/payments/${id}`)
}
