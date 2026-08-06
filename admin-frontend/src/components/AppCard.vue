<template>
  <div class="app-card" :class="{ 'app-card-hover': hoverable }">
    <div v-if="$slots.header" class="app-card-header">
      <slot name="header" />
    </div>
    <div class="app-card-body">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ hoverable?: boolean }>()
</script>

<script lang="ts">
export default { name: 'AppCard' }
</script>

<style scoped>
.app-card {
  background-color: var(--bg-secondary);
  border-radius: 20px;
  box-shadow: var(--shadow-md);
  transition: transform 0.3s cubic-bezier(0.25, 0.1, 0.25, 1.0), box-shadow 0.3s cubic-bezier(0.25, 0.1, 0.25, 1.0);
  overflow: hidden;
}
.app-card-hover:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}
.app-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}
.app-card-body {
  padding: 24px;
}
</style>
