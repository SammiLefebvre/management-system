import { useThemeStore } from '@/store/theme'
import { storeToRefs } from 'pinia'

export function useTheme() {
  const store = useThemeStore()
  const { isDark } = storeToRefs(store)
  return { isDark, toggleTheme: store.toggleTheme }
}
