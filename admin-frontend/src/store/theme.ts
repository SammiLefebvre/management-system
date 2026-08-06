import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(localStorage.getItem('theme') === 'dark')

  function applyTheme() {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('theme', 'light')
    }
  }

  function toggleTheme() {
    isDark.value = !isDark.value
    applyTheme()
  }

  // 初始化
  applyTheme()

  watch(isDark, applyTheme)

  return { isDark, toggleTheme, applyTheme }
})
