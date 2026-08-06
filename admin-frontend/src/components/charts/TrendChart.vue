<template>
  <v-chart class="chart" :option="option" autoresize />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import type { WorkOrderTrend } from '@/api/statistics'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const props = defineProps<{ data: WorkOrderTrend }>()

const option = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: 0 },
  grid: { left: 16, right: 16, top: 24, bottom: 32, containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: props.data.dates },
  yAxis: { type: 'value', minInterval: 1 },
  series: props.data.series.map(s => ({
    name: s.name,
    type: 'line',
    smooth: true,
    data: s.data,
    symbolSize: 6,
  })),
}))
</script>

<script lang="ts">
export default { name: 'TrendChart' }
</script>

<style scoped>
.chart { height: 260px; width: 100%; }
</style>
