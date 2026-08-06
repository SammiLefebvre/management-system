import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCodeTable, addCodeValue, updateCodeValue, deleteCodeValue } from '@/api/codetable'

export interface CodeItem {
  id?: number
  codeType: string
  codeValue: string
  codeLabel: string
  sortOrder: number
}

export const useCodeTableStore = defineStore('codeTable', () => {
  const cache = ref<Record<string, CodeItem[]>>({})
  const loading = ref<Record<string, boolean>>({})

  async function load(type: string): Promise<CodeItem[]> {
    if (cache.value[type]?.length) return cache.value[type]
    loading.value[type] = true
    try {
      const res = await getCodeTable(type)
      const list = (res.data || []).sort((a: CodeItem, b: CodeItem) => a.sortOrder - b.sortOrder)
      cache.value[type] = list
      return list
    } finally {
      loading.value[type] = false
    }
  }

  async function refresh(type: string): Promise<CodeItem[]> {
    delete cache.value[type]
    return load(type)
  }

  function getCached(type: string): CodeItem[] {
    return cache.value[type] || []
  }

  function getCodeLabel(type: string, value: string | null | undefined): string {
    if (!value) return '-'
    const item = getCached(type).find(item => item.codeValue === value)
    return item?.codeLabel ?? value
  }

  function getEmergencyLevels(): CodeItem[] {
    return getCached('emergency_level')
  }

  function getEmergencySeverity(level: string): number {
    const list = getEmergencyLevels()
    const index = list.findIndex(item => item.codeValue === level)
    return index === -1 ? list.length : index
  }

  function emergencyTagType(level: string): '' | 'danger' | 'warning' | 'info' {
    const severity = getEmergencySeverity(level)
    if (severity === 0) return 'danger'
    if (severity === 1) return 'warning'
    return 'info'
  }

  function getEmergencyLabel(level: string | null | undefined): string {
    return getCodeLabel('emergency_level', level)
  }

  // 便捷 mutation 方法，修改后自动刷新缓存
  async function add(type: string, data: Omit<CodeItem, 'id'>): Promise<void> {
    await addCodeValue({ ...data, codeType: type })
    await refresh(type)
  }

  async function update(type: string, data: CodeItem): Promise<void> {
    await updateCodeValue(data)
    await refresh(type)
  }

  async function remove(type: string, id: number): Promise<void> {
    await deleteCodeValue(id)
    await refresh(type)
  }

  return {
    cache,
    loading,
    load,
    refresh,
    getCached,
    getCodeLabel,
    getEmergencyLevels,
    getEmergencySeverity,
    emergencyTagType,
    getEmergencyLabel,
    add,
    update,
    remove,
  }
})
