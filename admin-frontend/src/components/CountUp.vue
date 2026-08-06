<template>
  <span>{{ displayValue }}</span>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

const props = defineProps<{
  value: number
  duration?: number
}>()

const displayValue = ref(0)

function easeOutExpo(t: number) {
  return t === 1 ? 1 : 1 - Math.pow(2, -10 * t)
}

function animate(from: number, to: number, duration: number) {
  const startTime = performance.now()
  function step(now: number) {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    displayValue.value = Math.floor(from + (to - from) * easeOutExpo(progress))
    if (progress < 1) {
      requestAnimationFrame(step)
    } else {
      displayValue.value = to
    }
  }
  requestAnimationFrame(step)
}

onMounted(() => animate(0, props.value, props.duration || 1200))
watch(() => props.value, (newVal, oldVal) => animate(oldVal ?? 0, newVal, props.duration || 1200))
</script>

<script lang="ts">
export default { name: 'CountUp' }
</script>
