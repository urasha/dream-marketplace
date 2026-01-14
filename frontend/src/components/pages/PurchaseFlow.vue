<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, CheckCircle, AlertCircle } from 'lucide-vue-next'
import { useLotsStore } from '../../stores/lots'

const props = defineProps({
  lotId: { type: Number, default: null },
  userBalance: { type: Number, required: true },
})

const emit = defineEmits(['updateBalance'])
const router = useRouter()
const lotsStore = useLotsStore()

const step = ref('confirm')
const selectedAmount = ref(null)

const lot = computed(() => lotsStore.state.current)
const hasSufficientBalance = computed(() => (lot.value ? props.userBalance >= lot.value.price : false))

onMounted(() => {
  if (props.lotId) {
    lotsStore.loadLot(props.lotId).catch(() => {})
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

const handleTopUp = () => {
  if (!selectedAmount.value) return
  step.value = 'processing'
  setTimeout(() => {
    emit('updateBalance', props.userBalance + selectedAmount.value)
    step.value = 'confirm'
    selectedAmount.value = null
  }, 1500)
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

      <button @click="step = 'topup'" class="w-full py-4 bg-black text-white hover:bg-gray-800 transition-colors">
        Пополнить баланс
      </button>
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

      <div class="p-4 bg-blue-50 border-2 border-blue-600 mb-8">
        <p class="text-gray-700">Это демо-экран. В бою здесь была бы интеграция с платёжной системой.</p>
      </div>

      <button
        @click="handleTopUp"
        :disabled="!selectedAmount"
        class="w-full py-4 bg-black text-white hover:bg-gray-800 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
      >
        {{ selectedAmount ? `Пополнить на ${selectedAmount} ₽` : 'Выберите сумму' }}
      </button>
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
