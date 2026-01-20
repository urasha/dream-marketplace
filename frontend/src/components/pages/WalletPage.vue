<script setup>
import { ref, computed, onMounted } from 'vue'
import { AlertCircle, CheckCircle } from 'lucide-vue-next'
import { createDeposit, fetchPaymentStatus, fetchWallet } from '../../api/payments'

const props = defineProps({
  userBalance: { type: Number, required: true },
})

const emit = defineEmits(['updateBalance'])

const amount = ref(500)
const paymentId = ref(null)
const paymentStatus = ref(null)
const loading = ref(false)
const checking = ref(false)
const error = ref('')

const balanceDisplay = computed(() => `${props.userBalance.toFixed ? props.userBalance.toFixed(2) : props.userBalance} ₽`)

onMounted(() => {
  const savedId = window.localStorage.getItem('dm_last_payment_id')
  if (savedId) {
    paymentId.value = savedId
    checkStatus()
  }
})

const startDeposit = async () => {
  error.value = ''
  if (!amount.value || Number(amount.value) < 1) {
    error.value = 'Минимальная сумма — 1 ₽'
    return
  }
  loading.value = true
  try {
    const payment = await createDeposit(Number(amount.value))
    if (!payment?.id || !payment?.confirmationUrl) {
      throw new Error('Не удалось создать платеж')
    }
    paymentId.value = payment.id
    paymentStatus.value = payment.status
    window.localStorage.setItem('dm_last_payment_id', payment.id)
    window.location.href = payment.confirmationUrl
  } catch (e) {
    error.value = e?.data?.message || e?.message || 'Ошибка создания платежа'
  } finally {
    loading.value = false
  }
}

const checkStatus = async () => {
  if (!paymentId.value) return
  checking.value = true
  error.value = ''
  try {
    const payment = await fetchPaymentStatus(paymentId.value)
    paymentStatus.value = payment?.status || null
    if (paymentStatus.value === 'SUCCEEDED') {
      window.localStorage.removeItem('dm_last_payment_id')
      const wallet = await fetchWallet().catch(() => null)
      const balance = wallet?.balance !== undefined ? Number(wallet.balance) : props.userBalance
      emit('updateBalance', balance)
    } else if (paymentStatus.value === 'CANCELED' || paymentStatus.value === 'FAILED') {
      window.localStorage.removeItem('dm_last_payment_id')
      error.value = 'Платеж отменен или не удался'
    }
  } catch (e) {
    error.value = e?.data?.message || 'Не удалось проверить статус'
  } finally {
    checking.value = false
  }
}
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <h1 class="mb-6 page-title">Баланс и пополнение</h1>

    <div class="p-6 bg-white border border-gray-200 rounded-xl shadow-sm mb-6">
      <div class="text-gray-600 mb-2">Текущий баланс</div>
      <div class="text-3xl font-semibold text-violet-700">{{ balanceDisplay }}</div>
    </div>

    <div class="p-6 bg-white border border-gray-200 rounded-xl shadow-sm space-y-4">
      <h2 class="text-lg font-semibold">Пополнить баланс</h2>
      <div class="grid grid-cols-2 gap-3">
        <button
          v-for="preset in [300, 500, 1000, 2000]"
          :key="preset"
          @click="amount = preset"
          class="px-4 py-3 border rounded-lg text-left transition-colors"
          :class="amount === preset ? 'border-violet-500 bg-violet-50' : 'border-gray-200 hover:border-gray-400'"
        >
          {{ preset }} ₽
        </button>
      </div>
      <div>
        <label class="block mb-2 text-gray-700">Своя сумма</label>
        <input
          v-model.number="amount"
          type="number"
          min="1"
          step="1"
          class="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent"
        />
      </div>

      <button
        @click="startDeposit"
        :disabled="loading"
        class="w-full py-3 bg-violet-600 text-white rounded-lg hover:bg-violet-700 transition-colors disabled:opacity-60"
      >
        {{ loading ? 'Создаём платёж...' : `Перейти к оплате ${amount || 0} ₽` }}
      </button>

      <div
        v-if="paymentId"
        class="flex items-center justify-between px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg"
      >
        <div>
          <div class="text-sm text-gray-600">Последний платеж</div>
          <div class="text-gray-900 text-sm break-all">{{ paymentId }}</div>
          <div class="text-xs text-gray-500">Статус: {{ paymentStatus || 'PENDING' }}</div>
        </div>
        <button
          @click="checkStatus"
          :disabled="checking"
          class="px-4 py-2 text-sm bg-white border border-gray-300 rounded-lg hover:border-black disabled:opacity-60"
        >
          {{ checking ? 'Проверяем...' : 'Проверить статус' }}
        </button>
      </div>

      <div v-if="error" class="flex items-start gap-2 p-3 text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg">
        <AlertCircle class="w-4 h-4 mt-0.5" />
        <span>{{ error }}</span>
      </div>

      <div v-else-if="paymentStatus === 'SUCCEEDED'" class="flex items-start gap-2 p-3 text-sm text-green-700 bg-green-50 border border-green-200 rounded-lg">
        <CheckCircle class="w-4 h-4 mt-0.5" />
        <span>Платёж успешно завершён, баланс обновлён.</span>
      </div>
    </div>
  </div>
</template>
