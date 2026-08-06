import { useCodeTableStore } from '@/store/codeTable'
import { storeToRefs } from 'pinia'

export interface CodeItem {
  id?: number
  codeType: string
  codeValue: string
  codeLabel: string
  sortOrder: number
}

export function useCodeTable() {
  const store = useCodeTableStore()
  const { cache, loading } = storeToRefs(store)

  return {
    cache,
    loading,
    load: store.load,
    refresh: store.refresh,
    getCached: store.getCached,
    getCodeLabel: store.getCodeLabel,
    getEmergencyLevels: store.getEmergencyLevels,
    getEmergencySeverity: store.getEmergencySeverity,
    emergencyTagType: store.emergencyTagType,
    getEmergencyLabel: store.getEmergencyLabel,
    add: store.add,
    update: store.update,
    remove: store.remove,
  }
}
