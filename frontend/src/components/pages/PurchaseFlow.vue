<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, CheckCircle, AlertCircle } from 'lucide-vue-next'
import { useLotsStore } from '../../stores/lots'
import { createDeposit, fetchPaymentStatus, fetchWallet } from '../../api/payments'

const props = defineProps({
  lotId: { type: Number, default: null },
  userBalance: { type: Number, required: true },
})

const emit = defineEmits(['updateBalance'])
const router = useRouter()
const lotsStore = useLotsStore()

const step = ref('confirm')
const selectedAmount = ref(null)
const paymentId = ref(null)
const paymentStatus = ref(null)
const paymentChecking = ref(false)
const paymentError = ref('')

const lot = computed(() => lotsStore.state.current)
const hasSufficientBalance = computed(() => (lot.value ? props.userBalance >= lot.value.price : false))

onMounted(() => {
  if (props.lotId) {
    lotsStore.loadLot(props.lotId).catch(() => {})
  }

  const savedId = window.localStorage.getItem('dm_last_payment_id')
  if (savedId) {
    paymentId.value = savedId
    step.value = 'topup'
    checkPaymentStatus()
  }
})

const handleConfirmPurchase = () => {
  if (!lot.value) return
  if (hasSufficientBalance.value) {
    step.value = 'processing'
    setTimeout(() => {
      emit('updateBalance', props.userBalance - lot.value.price)
      step.value = 'success'
    }, 1500)
  } else {
    step.value = 'insufficient'
  }
}

const handleTopUp = async () => {
  if (!selectedAmount.value) return
  paymentError.value = ''
  step.value = 'processing'
  try {
    const payment = await createDeposit(selectedAmount.value)
    if (!payment?.id || !payment?.confirmationUrl) {
      throw new Error('Не удалось создать платеж')
    }
    paymentId.value = payment.id
    paymentStatus.value = payment.status
    window.localStorage.setItem('dm_last_payment_id', payment.id)
    window.location.href = payment.confirmationUrl
  } catch (e) {
    paymentError.value = e?.data?.message || e?.message || 'Ошибка создания платежа'
    step.value = 'topup'
  }
}

const checkPaymentStatus = async () => {
  if (!paymentId.value) return
  paymentChecking.value = true
  paymentError.value = ''
  try {
    const payment = await fetchPaymentStatus(paymentId.value)
    paymentStatus.value = payment?.status || null
    if (paymentStatus.value === 'SUCCEEDED') {
      window.localStorage.removeItem('dm_last_payment_id')
      const wallet = await fetchWallet().catch(() => null)
      const balance = wallet?.balance !== undefined ? Number(wallet.balance) : props.userBalance
      emit('updateBalance', balance)
      selectedAmount.value = null
      step.value = 'confirm'
    } else if (paymentStatus.value === 'CANCELED' || paymentStatus.value === 'FAILED') {
      window.localStorage.removeItem('dm_last_payment_id')
      paymentError.value = 'Платеж отменен или не удался'
    }
  } catch (e) {
    paymentError.value = e?.data?.message || 'Не удалось проверить статус'
  } finally {
    paymentChecking.value = false
  }
}
</script>

<template>
  <div class="max-w-[800px] mx-auto px-6 py-12" v-if="lot">
    <template v-if="step === 'confirm'">
      <button
        @click="router.push({ name: 'lot-detail', params: { id: lotId } })"
        class="flex items-center gap-2 mb-6 text-gray-600 hover:text-black transition-colors"
      >
        <ArrowLeft class="w-5 h-5" />
        Назад к лоту
      </button>

      <h1 class="mb-8">Подтверждение покупки</h1>

      <div class="p-6 bg-gray-50 border-2 border-gray-300 mb-8">
        <h2 class="mb-4">{{ lot.title }}</h2>
        <div class="flex items-center justify-between mb-4">
          <span class="text-gray-600">Автор:</span>
          <span>{{ lot.author }}</span>
        </div>
        <div class="flex items-center justify-between mb-4">
          <span class="text-gray-600">Лицензия:</span>
          <span>{{ lot.license }}</span>
        </div>
        <div class="flex items-center justify-between pt-4 border-t-2 border-gray-300">
          <span>Цена:</span>
          <span>{{ lot.price }} ₽</span>
        </div>
      </div>

      <div class="p-6 bg-white border-2 border-gray-300 mb-8">
        <div class="flex items-center justify-between mb-4">
          <span class="text-gray-600">Ваш баланс:</span>
          <span :class="hasSufficientBalance ? '' : 'text-red-600'">{{ userBalance }} ₽</span>
        </div>
        <div class="flex items-center justify-between pt-4 border-t-2 border-gray-300">
          <span>Баланс после покупки:</span>
          <span :class="hasSufficientBalance ? '' : 'text-red-600'">
            {{ hasSufficientBalance ? userBalance - lot.price : userBalance }} ₽
          </span>
        </div>
      </div>

      <div v-if="!hasSufficientBalance" class="p-4 bg-yellow-50 border-2 border-yellow-600 mb-8 flex items-start gap-3">
        <AlertCircle class="w-6 h-6 flex-shrink-0" />
        <div>
          <h3>Недостаточно средств</h3>
          <p class="text-gray-700">
            Для покупки этого лота необходимо пополнить баланс на {{ lot.price - userBalance }} ₽
          </p>
        </div>
      </div>

      <div class="flex gap-4">
        <button
          @click="handleConfirmPurchase"
          :disabled="!hasSufficientBalance"
          class="px-8 py-3 bg-black text-white hover:bg-gray-800 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          Подтвердить покупку
        </button>
        <button
          @click="router.push({ name: 'lot-detail', params: { id: lotId } })"
          class="px-8 py-3 border-2 border-gray-400 hover:border-black transition-colors"
        >
          Отмена
        </button>
      </div>
    </template>

    <template v-else-if="step === 'insufficient'">
      <h1 class="mb-8">Пополнение баланса</h1>

      <div class="p-6 bg-red-50 border-2 border-red-600 mb-8">
        <h2 class="mb-2">Недостаточно средств</h2>
        <p class="text-gray-700">
          Ваш текущий баланс: {{ userBalance }} ₽<br />
          Необходимо для покупки: {{ lot.price }} ₽<br />
          Нужно пополнить: {{ lot.price - userBalance }} ₽
        </p>
      </div>

      <div class="space-y-4">
        <button @click="step = 'topup'" class="w-full py-4 bg-black text-white hover:bg-gray-800 transition-colors">
          Пополнить баланс
        </button>
        <div v-if="paymentError" class="p-3 text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg">
          {{ paymentError }}
        </div>
      </div>
    </template>

    <template v-else-if="step === 'topup'">
      <button
        @click="step = 'confirm'"
        class="flex items-center gap-2 mb-6 text-gray-600 hover:text-black transition-colors"
      >
        <ArrowLeft class="w-5 h-5" />
        Назад
      </button>

      <h1 class="mb-8">Пополнение баланса</h1>

      <div class="p-6 bg-gray-50 border-2 border-gray-300 mb-8">
        <div class="text-gray-600 mb-2">Текущий баланс:</div>
        <div>{{ userBalance }} ₽</div>
      </div>

      <h2 class="mb-4">Выберите сумму пополнения</h2>
      <div class="grid grid-cols-2 gap-4 mb-8">
        <button
          v-for="amount in [100, 500, 1000, 2000]"
          :key="amount"
          @click="selectedAmount = amount"
          class="p-6 border-2 transition-colors text-center"
          :class="selectedAmount === amount ? 'bg-black text-white border-black' : 'border-gray-300 hover:border-black'"
        >
          <div class="mb-2">{{ amount }} ₽</div>
          <div class="text-gray-400">Баланс станет: {{ userBalance + amount }} ₽</div>
        </button>
      </div>

      <div class="space-y-3">
        <button
          @click="handleTopUp"
          :disabled="!selectedAmount || paymentChecking"
          class="w-full py-4 bg-black text-white hover:bg-gray-800 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          {{ paymentChecking ? 'Создаём платёж...' : selectedAmount ? `Перейти к оплате ${selectedAmount} ₽` : 'Выберите сумму' }}
        </button>

        <div class="flex items-center justify-between px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg" v-if="paymentId">
          <div>
            <div class="text-sm text-gray-600">Последний платеж</div>
            <div class="text-gray-900 text-sm">{{ paymentId }}</div>
            <div class="text-xs text-gray-500">Статус: {{ paymentStatus || 'PENDING' }}</div>
          </div>
          <button
            @click="checkPaymentStatus"
            :disabled="paymentChecking"
            class="px-4 py-2 text-sm bg-white border border-gray-300 rounded-lg hover:border-black disabled:opacity-60"
          >
            {{ paymentChecking ? 'Проверяем...' : 'Проверить статус' }}
          </button>
        </div>

        <div v-if="paymentError" class="p-3 text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg">
          {{ paymentError }}
        </div>
      </div>
    </template>

    <template v-else-if="step === 'processing'">
      <div class="p-12 text-center border-2 border-gray-300">
        <div class="w-16 h-16 border-4 border-gray-300 border-t-black rounded-full animate-spin mx-auto mb-4" />
        <h2>Обработка платежа...</h2>
      </div>
    </template>

    <template v-else-if="step === 'success'">
      <div class="p-12 text-center border-2 border-green-600 bg-green-50">
        <CheckCircle class="w-16 h-16 mx-auto mb-4" />
        <h1 class="mb-4">Покупка завершена</h1>
        <p class="text-gray-700 mb-8">Транзакция выполнена успешно. Лот добавлен в ваши покупки.</p>
        <button
          @click="router.push({ name: 'lot-detail', params: { id: lotId } })"
          class="px-8 py-3 bg-black text-white hover:bg-gray-800 transition-colors"
        >
          Перейти к скачиванию
        </button>
      </div>
    </template>
  </div>

  <div v-else class="max-w-[800px] mx-auto px-6 py-12">
    <p>Лот не найден</p>
  </div>
</template>
