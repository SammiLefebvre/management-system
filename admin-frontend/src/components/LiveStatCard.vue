<template>
  <AppCard class="live-stat-card" :class="pulseClass">
    <template #header>
      <span>{{ title }}</span>
      <span v-if="subtitle" class="card-date">{{ subtitle }}</span>
    </template>
    <div class="stat-primary">
      <CountUp :value="value" />
      <span v-if="deltaText" class="stat-delta">{{ deltaText }}</span>
    </div>
    <slot />
  </AppCard>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import AppCard from './AppCard.vue'
import CountUp from './CountUp.vue'

const props = defineProps<{
  title: string
  value: number
  subtitle?: string
  deltaText?: string
}>()

const pulseClass = ref('')
watch(() => props.value, (newVal, oldVal) => {
  if (oldVal === undefined) return
  pulseClass.value = newVal > oldVal ? 'pulse-up' : newVal < oldVal ? 'pulse-down' : ''
  setTimeout(() => (pulseClass.value = ''), 1000)
})
</script>

<script lang="ts">
export default { name: 'LiveStatCard' }
</script>

<style scoped>
.live-stat-card {
  transition: box-shadow 0.3s ease, border-color 0.3s ease;
}
.pulse-up {
  box-shadow: 0 0 0 4px rgba(52, 199, 89, 0.2);
}
.pulse-down {
  box-shadow: 0 0 0 4px rgba(255, 59, 48, 0.2);
}
</style>
