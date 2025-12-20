<script setup>
import { Lock, Unlock } from 'lucide-vue-next'

const props = defineProps({
  id: { type: Number, required: true },
  title: { type: String, required: true },
  date: { type: String, required: true },
  tags: { type: Array, default: () => [] },
  isPrivate: { type: Boolean, default: false },
})

const emit = defineEmits(['click'])
</script>

<template>
  <button
    class="w-full text-left p-4 bg-white border border-gray-200 rounded-xl hover:shadow-lg hover:border-violet-300 transition-all"
    @click="emit('click')"
  >
    <div class="flex items-start justify-between mb-2">
      <h3 class="flex-1">{{ title }}</h3>
      <div class="ml-3">
        <Lock v-if="isPrivate" class="w-4 h-4 text-gray-400" />
        <Unlock v-else class="w-4 h-4 text-green-500" />
      </div>
    </div>

    <div class="text-gray-600 mb-3">{{ date }}</div>

    <div class="flex flex-wrap gap-2">
      <span
        v-for="(tag, index) in tags"
        :key="index"
        class="px-2 py-1 bg-indigo-50 text-indigo-700 rounded-md"
      >
        {{ tag }}
      </span>
    </div>
  </button>
</template>
