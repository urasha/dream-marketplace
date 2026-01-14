<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import LotCard from '../cards/LotCard.vue'
import { useLotsStore } from '../../stores/lots'

const router = useRouter()
const lotsStore = useLotsStore()

const displayedLots = computed(() => lotsStore.state.catalog)
const loading = computed(() => lotsStore.state.loading)

onMounted(() => {
  lotsStore.loadCatalog()
})
</script>

<template>
  <div class="max-w-[1160px] mx-auto px-6 py-12">
    <h1 class="mb-8">Маркетплейс снов</h1>

    <div v-if="loading" class="text-gray-600">Загружаем лоты...</div>
    <div v-else-if="!displayedLots.length" class="text-gray-600">Лотов пока нет</div>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
      <LotCard
        v-for="lot in displayedLots"
        :key="lot.id"
        :title="lot.title"
        :description="lot.description"
        :price="lot.price"
        :author="lot.authorName"
        @click="() => router.push({ name: 'lot-detail', params: { id: lot.id } })"
      />
    </div>
  </div>
</template>
