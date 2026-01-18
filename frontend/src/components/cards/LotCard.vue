<script setup>
import { Star } from 'lucide-vue-next'

const props = defineProps({
  id: { type: Number, required: true },
  title: { type: String, required: true },
  description: { type: String, default: '' },
  price: { type: [Number, String], default: null },
  author: { type: String, default: 'Автор' },
  category: { type: String, default: '' },
  imageUrl: { type: String, default: '' },
  ratingAverage: { type: Number, default: 0 },
  ratingCount: { type: Number, default: 0 },
  tags: { type: Array, default: () => [] },
})

const emit = defineEmits(['click'])
</script>

<template>
  <button
    class="block w-full align-top text-left bg-white border border-gray-200 rounded-xl
           transition-all hover:shadow-lg hover:border-violet-300
           overflow-hidden"
    @click="emit('click')"
  >
    <div class="w-full aspect-[4/3] overflow-hidden rounded-t-xl bg-gray-100">
      <img
        v-if="imageUrl"
        :src="imageUrl"
        alt="lot preview"
        class="w-full h-full object-cover object-center block"
        loading="lazy"
        draggable="false"
      />
      <div
        v-else
        class="h-full w-full flex items-center justify-center
               bg-gradient-to-br from-violet-100 via-purple-50 to-indigo-100
               text-violet-400 text-sm"
      >
        400×300
      </div>
    </div>

    <!-- Content -->
    <div class="p-4 border-t border-gray-200">
      <h3 class="mb-2 font-semibold leading-snug line-clamp-2 h-[2.5rem] overflow-hidden">
        {{ title }}
      </h3>

        <p class="mb-3 text-gray-600 text-sm line-clamp-2 h-[2.5rem] overflow-hidden">
        {{ description }}
      </p>

      <div
        v-if="category"
        class="mb-2 text-xs font-semibold uppercase tracking-wide text-violet-700"
      >
        {{ category }}
      </div>

      <div class="flex flex-wrap gap-2 mb-3 h-[3.5rem] overflow-hidden">
        <span
          v-for="(tag, index) in tags"
          :key="index"
          class="px-2 py-1 text-xs rounded-md
                 bg-violet-50 text-violet-700"
        >
          {{ tag }}
        </span>
      </div>

      <div class="flex items-center justify-between pt-3 border-t border-gray-100">
        <div class="flex items-center gap-1 text-sm">
          <Star class="w-4 h-4 fill-amber-400 text-amber-400" />
          <span class="text-gray-800">
            {{ (ratingAverage || 0).toFixed(1) }}
          </span>
          <span v-if="ratingCount" class="text-gray-500 text-xs">
            ({{ ratingCount }})
          </span>
        </div>

        <div class="text-sm text-gray-600 truncate max-w-[50%]">
          {{ author }}
        </div>
      </div>

      <div class="mt-3 font-semibold text-violet-600">
        {{ price ? `${price} ₽` : '—' }}
      </div>
    </div>
  </button>
</template>
