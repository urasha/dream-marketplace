<script setup>
import { Star } from 'lucide-vue-next'

const props = defineProps({
  id: { type: Number, required: true },
  title: { type: String, required: true },
  description: { type: String, default: '' },
  price: { type: [Number, String], default: null },
  author: { type: String, default: 'Автор' },
  category: { type: String, default: '' },
  ratingAverage: { type: Number, default: 0 },
  ratingCount: { type: Number, default: 0 },
  tags: { type: Array, default: () => [] },
})

const emit = defineEmits(['click'])
</script>

<template>
  <button
    class="w-full text-left bg-white border border-gray-200 rounded-xl hover:shadow-lg hover:border-violet-300 transition-all overflow-hidden"
    @click="emit('click')"
  >
    <div class="w-full aspect-[4/3] bg-gradient-to-br from-violet-100 via-purple-50 to-indigo-100 border-b border-gray-200 flex items-center justify-center">
      <div class="text-violet-400">400×300</div>
    </div>

    <div class="p-4">
      <h3 class="mb-2">{{ title }}</h3>
      <p class="text-gray-600 mb-3 line-clamp-2">{{ description }}</p>

      <div v-if="category" class="mb-2 text-xs font-semibold uppercase text-violet-700">
        {{ category }}
      </div>

      <div class="flex flex-wrap gap-2 mb-3">
        <span
          v-for="(tag, index) in tags"
          :key="index"
          class="px-2 py-1 bg-violet-50 text-violet-700 rounded-md"
        >
          {{ tag }}
        </span>
      </div>

      <div class="flex items-center justify-between pt-3 border-t border-gray-200">
        <div class="flex items-center gap-1">
          <Star class="w-4 h-4 fill-amber-400 text-amber-400" />
          <span class="text-gray-800">{{ (ratingAverage || 0).toFixed(1) }}</span>
          <span class="text-gray-500 text-xs" v-if="ratingCount">({{ ratingCount }})</span>
        </div>
        <div class="text-gray-600">{{ author }}</div>
      </div>

      <div class="mt-3 text-violet-600">{{ price ? `${price} ₽` : '—' }}</div>
    </div>
  </button>
</template>
