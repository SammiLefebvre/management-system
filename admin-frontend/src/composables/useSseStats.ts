import { ref, onMounted, onUnmounted } from 'vue'
import { getDashboardStatistics, type DashboardStatistics } from '@/api/statistics'

const defaultStats: DashboardStatistics = {
  workOrderTotal: 0,
  workOrderToday: 0,
  statusCounts: {},
  slaOverdueCount: 0,
  deviceTotal: 0,
  personnelTotal: 0,
  teamTotal: 0,
  last7Days: [],
}

export function useSseStats() {
  const stats = ref<DashboardStatistics>(defaultStats)
  let es: EventSource | null = null
  let timer: number | null = null
  const base = 'http://localhost:9090'
  const token = localStorage.getItem('token')

  function connect() {
    if (!token) return
    es = new EventSource(`${base}/api/sse/dashboard?token=${token}`)
    es.addEventListener('stats', (e) => {
      stats.value = JSON.parse(e.data)
    })
    es.onerror = () => {
      es?.close()
      startPolling()
    }
  }

  function startPolling() {
    if (timer) return
    timer = window.setInterval(async () => {
      try {
        const res = await getDashboardStatistics()
        stats.value = res.data
      } catch { /* handled by interceptor */ }
    }, 30000)
  }

  onMounted(() => {
    if (typeof EventSource !== 'undefined') connect()
    else startPolling()
  })
  onUnmounted(() => {
    es?.close()
    if (timer) clearInterval(timer)
  })

  return { stats }
}
