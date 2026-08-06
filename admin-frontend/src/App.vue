<template>
  <router-view v-slot="{ Component }">
    <transition name="page" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useThemeStore } from '@/store/theme'

onMounted(() => {
  useThemeStore().applyTheme()
})
</script>

<style>
body { margin: 0; padding: 0; background: var(--bg-primary); }
#app { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
</style>
