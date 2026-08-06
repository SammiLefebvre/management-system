<template>
  <div class="skeleton-container">
    <div
      v-for="i in rows"
      :key="i"
      class="skeleton-row animate-shimmer"
      :style="{ height: rowHeight + 'px', marginBottom: gap + 'px' }"
    />
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ rows?: number; rowHeight?: number; gap?: number }>(), {
  rows: 6,
  rowHeight: 20,
  gap: 12,
})
</script>

<script lang="ts">
export default { name: 'AppSkeleton' }
</script>

<style scoped>
.skeleton-row {
  border-radius: 8px;
  width: 100%;
}
</style>
