<template>
  <button class="theme-toggle" @click="toggleTheme" :title="isDark ? '切换浅色' : '切换深色'">
    <el-icon v-if="isDark" size="18"><Sunny /></el-icon>
    <el-icon v-else size="18"><Moon /></el-icon>
  </button>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'
import { Sunny, Moon } from '@element-plus/icons-vue'

const { isDark, toggleTheme } = useTheme()
</script>

<script lang="ts">
export default { name: 'ThemeToggle' }
</script>

<style scoped>
.theme-toggle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: var(--bg-tertiary);
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}
.theme-toggle:hover {
  background: var(--accent-light);
  color: var(--accent);
}
</style>
