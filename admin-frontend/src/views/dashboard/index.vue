<template>
  <div class="dashboard-page">
    <div class="dashboard-hero">
      <div>
        <h2 class="hero-title">你好，{{ userStore.userInfo.account || '管理员' }}</h2>
        <p class="hero-subtitle">{{ summaryText }}</p>
      </div>
      <el-button type="primary" size="large" round @click="$router.push('/work-order/create')">
        新建工单
      </el-button>
    </div>

    <div v-if="loading" class="dashboard-grid">
      <AppCard v-for="i in 4" :key="i" class="dashboard-card" :class="skeletonSpan(i)">
        <AppSkeleton :rows="5" :row-height="18" />
      </AppCard>
    </div>

    <div v-else class="dashboard-grid">
      <!-- 工单总览 -->
      <LiveStatCard class="dashboard-card card-overview" title="工单总数" :value="stats.workOrderTotal" :delta-text="`+${stats.workOrderToday} 今日新增`">
        <div class="trend-labels">
          <span v-for="item in stats.last7Days.slice(-5)" :key="item.date" class="trend-date">
            {{ formatDay(item.date) }}
          </span>
        </div>
        <MiniTrend :data="stats.last7Days" />
      </LiveStatCard>

      <!-- SLA 预警 -->
      <LiveStatCard class="dashboard-card card-sla" title="SLA 预警" :value="stats.slaOverdueCount" />

      <!-- 工单状态分布 -->
      <AppCard class="dashboard-card card-status" :hoverable="true">
        <template #header>
          <span>工单状态</span>
        </template>
        <div class="status-bars">
          <div
            v-for="item in statusItems"
            :key="item.key"
            class="status-bar-item"
          >
            <div class="status-bar-header">
              <StatusTag :type="item.type" :label="item.label" />
              <span class="status-bar-value">{{ item.value }}</span>
            </div>
            <div class="status-bar-track">
              <div
                class="status-bar-fill"
                :style="{ width: item.percent + '%', background: item.color }"
              />
            </div>
          </div>
        </div>
      </AppCard>

      <!-- 资源统计 -->
      <AppCard class="dashboard-card card-resources" :hoverable="true">
        <template #header>
          <span>资源统计</span>
        </template>
        <div class="resource-grid">
          <div v-for="r in resources" :key="r.label" class="resource-item">
            <el-icon size="24" :color="r.color"><component :is="r.icon" /></el-icon>
            <div class="resource-value"><CountUp :value="r.value" /></div>
            <div class="resource-label">{{ r.label }}</div>
          </div>
        </div>
      </AppCard>

      <!-- 近 30 天趋势 -->
      <AppCard class="dashboard-card card-trend" :hoverable="true">
        <template #header><span>近 30 天趋势</span></template>
        <TrendChart :data="trends" />
      </AppCard>

      <!-- 人员负载 -->
      <AppCard class="dashboard-card card-workload" :hoverable="true">
        <template #header><span>人员负载</span></template>
        <WorkloadChart :data="workload" />
      </AppCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import AppCard from '@/components/AppCard.vue'
import AppSkeleton from '@/components/AppSkeleton.vue'
import CountUp from '@/components/CountUp.vue'
import MiniTrend from '@/components/MiniTrend.vue'
import StatusTag from '@/components/StatusTag.vue'
import LiveStatCard from '@/components/LiveStatCard.vue'
import TrendChart from '@/components/charts/TrendChart.vue'
import WorkloadChart from '@/components/charts/WorkloadChart.vue'
import { useSseStats } from '@/composables/useSseStats'
import { getTrends, getWorkload, type WorkOrderTrend, type WorkloadItem } from '@/api/statistics'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const { stats } = useSseStats()
const loading = ref(true)
const trends = ref<WorkOrderTrend>({ dates: [], series: [] })
const workload = ref<WorkloadItem[]>([])

const summaryText = computed(() => {
  if (stats.value.slaOverdueCount > 0) {
    return `今日新增 ${stats.value.workOrderToday} 个工单，${stats.value.slaOverdueCount} 个工单已超期`
  }
  return `今日新增 ${stats.value.workOrderToday} 个工单，暂无超期工单`
})

const statusItems = computed(() => {
  const statusConfig = [
    { key: 'published', label: '待认领', type: 'info' as const, color: '#0071e3' },
    { key: 'claimed', label: '进行中', type: 'warning' as const, color: '#ff9500' },
    { key: 'in_progress', label: '作业中', type: 'warning' as const, color: '#ff9500' },
    { key: 'pending_confirm', label: '待确认', type: 'purple' as const, color: '#af52de' },
    { key: 'confirmed', label: '已确认', type: 'success' as const, color: '#34c759' },
  ]
  const max = Math.max(...statusConfig.map(s => stats.value.statusCounts[s.key] || 0), 1)
  return statusConfig.map(s => {
    const value = stats.value.statusCounts[s.key] || 0
    return { ...s, value, percent: (value / max) * 100 }
  })
})

const resources = computed(() => [
  { label: '设备', value: stats.value.deviceTotal, icon: 'Monitor', color: '#0071e3' },
  { label: '人员', value: stats.value.personnelTotal, icon: 'User', color: '#34c759' },
  { label: '班组', value: stats.value.teamTotal, icon: 'Avatar', color: '#af52de' },
])

function skeletonSpan(i: number) {
  if (i === 1) return 'card-overview'
  if (i === 2) return 'card-sla'
  if (i === 3) return 'card-status'
  return 'card-resources'
}

function formatDay(dateStr: string) {
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

onMounted(async () => {
  try {
    const [tRes, wRes] = await Promise.all([getTrends(30), getWorkload()])
    trends.value = tRes.data
    workload.value = wRes.data
  } catch (e) {
    ElMessage.error('加载图表数据失败')
  } finally {
    loading.value = false
  }
})
</script>

<script lang="ts">
export default { name: 'DashboardPage' }
</script>

<style scoped>
.dashboard-page {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
}
.dashboard-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 32px;
}
.hero-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px;
  color: var(--text-primary);
  letter-spacing: -0.03em;
}
.hero-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0;
}
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  grid-template-rows: auto auto auto auto;
  grid-template-areas:
    "overview overview overview overview overview overview overview overview sla sla sla sla"
    "overview overview overview overview overview overview overview overview status status status status"
    "resources resources resources resources resources resources resources resources resources resources resources resources"
    "trend trend trend trend trend trend trend trend workload workload workload workload";
  gap: 24px;
}
.dashboard-card {
  min-height: 220px;
}
.card-overview { grid-area: overview; }
.card-sla { grid-area: sla; }
.card-status { grid-area: status; }
.card-resources { grid-area: resources; }
.card-trend { grid-area: trend; }
.card-workload { grid-area: workload; }
@media (max-width: 1200px) {
  .dashboard-grid {
    grid-template-columns: 1fr 1fr;
    grid-template-areas:
      "overview overview"
      "sla status"
      "resources resources"
      "trend workload";
  }
}
@media (max-width: 768px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
    grid-template-areas:
      "overview"
      "sla"
      "status"
      "resources"
      "trend"
      "workload";
  }
}
.stat-primary {
  font-size: 48px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.03em;
  margin-bottom: 8px;
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.stat-primary.danger {
  color: var(--danger);
}
.stat-delta {
  font-size: 14px;
  color: var(--success);
  font-weight: 600;
}
.card-date {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 400;
}
.warning-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--danger);
  box-shadow: 0 0 0 4px rgba(255,59,48,0.2);
}
.card-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 12px;
}
.trend-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  margin-bottom: 6px;
}
.trend-date {
  font-size: 11px;
  color: var(--text-tertiary);
  flex: 1;
  text-align: center;
}
.status-bars {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.status-bar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.status-bar-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.status-bar-track {
  height: 8px;
  background: var(--border);
  border-radius: 4px;
  overflow: hidden;
}
.status-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 1s cubic-bezier(0.25, 0.1, 0.25, 1.0);
}
.resource-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
.resource-item {
  text-align: center;
  padding: 20px 16px;
  border-radius: 16px;
  background: var(--bg-primary);
  transition: transform 0.2s ease;
}
.resource-item:hover {
  transform: translateY(-2px);
}
.resource-value {
  font-size: 28px;
  font-weight: 700;
  margin: 12px 0 4px;
  color: var(--text-primary);
}
.resource-label {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
