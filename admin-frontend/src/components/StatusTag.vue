<template>
  <span class="status-tag" :class="typeClass">
    <span class="status-dot" />
    <span>{{ label }}</span>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ type: 'success' | 'warning' | 'danger' | 'info' | 'purple'; label: string }>()

const typeClass = computed(() => `status-tag--${props.type}`)
</script>

<script lang="ts">
export default { name: 'StatusTag' }
</script>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 500;
}
.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
.status-tag--success { background: rgba(52,199,89,0.12); color: var(--success); }
.status-tag--success .status-dot { background: var(--success); }
.status-tag--warning { background: rgba(255,149,0,0.12); color: var(--warning); }
.status-tag--warning .status-dot { background: var(--warning); }
.status-tag--danger { background: rgba(255,59,48,0.12); color: var(--danger); }
.status-tag--danger .status-dot { background: var(--danger); }
.status-tag--info { background: rgba(0,113,227,0.12); color: var(--accent); }
.status-tag--info .status-dot { background: var(--accent); }
.status-tag--purple { background: rgba(175,82,222,0.12); color: #af52de; }
.status-tag--purple .status-dot { background: #af52de; }
</style>
