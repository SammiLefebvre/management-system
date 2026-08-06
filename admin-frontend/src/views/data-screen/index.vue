<template>
  <div class="data-screen">
    <AppCard class="screen-header">
      <div class="screen-title">数据大屏</div>
      <div class="screen-filters">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" />
        <el-select v-model="view" placeholder="视图">
          <el-option label="地图" value="map" />
          <el-option label="故障热力" value="heatmap" />
          <el-option label="人员负载" value="workload" />
        </el-select>
      </div>
    </AppCard>

    <div class="screen-body">
      <div class="screen-sidebar">
        <LiveStatCard title="设备总数" :value="stats.deviceTotal" />
        <LiveStatCard title="在修设备" :value="inProgressDevices" />
        <LiveStatCard title="SLA 预警" :value="stats.slaOverdueCount" />
        <LiveStatCard title="人员总数" :value="stats.personnelTotal" />
      </div>
      <AppCard class="screen-main">
        <MapBoard v-if="view === 'map'" :devices="devices" />
        <HeatmapChart v-else-if="view === 'heatmap'" :data="heatmap" />
        <WorkloadChart v-else-if="view === 'workload'" :data="workload" />
      </AppCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AppCard from '@/components/AppCard.vue'
import LiveStatCard from '@/components/LiveStatCard.vue'
import MapBoard from '@/components/map/MapBoard.vue'
import HeatmapChart from '@/components/charts/HeatmapChart.vue'
import WorkloadChart from '@/components/charts/WorkloadChart.vue'
import { useSseStats } from '@/composables/useSseStats'
import { getDevicesWithLocation, getHeatmap, getWorkload, type DeviceMapItem, type HeatmapData, type WorkloadItem } from '@/api/statistics'

const { stats } = useSseStats()
const view = ref('map')
const dateRange = ref([])
const devices = ref<DeviceMapItem[]>([])
const heatmap = ref<HeatmapData>({ xAxis: [], yAxis: [], data: [] })
const workload = ref<WorkloadItem[]>([])

const inProgressDevices = computed(() => devices.value.filter(d => ['claimed','in_progress','completing'].includes(d.latestWorkOrderStatus || '')).length)

onMounted(async () => {
  const [d, h, w] = await Promise.all([getDevicesWithLocation(), getHeatmap(), getWorkload()])
  devices.value = d.data
  heatmap.value = h.data
  workload.value = w.data
})
</script>

<script lang="ts">
export default { name: 'DataScreen' }
</script>

<style scoped>
.data-screen { display: flex; flex-direction: column; gap: 24px; height: calc(100vh - 160px); }
.screen-header { display: flex; justify-content: space-between; align-items: center; }
.screen-title { font-size: 22px; font-weight: 700; }
.screen-filters { display: flex; gap: 12px; }
.screen-body { flex: 1; display: flex; gap: 24px; min-height: 0; }
.screen-sidebar { width: 320px; display: flex; flex-direction: column; gap: 16px; }
.screen-main { flex: 1; min-width: 0; overflow: hidden; }
</style>
