<template>
  <v-chart class="chart" :option="option" autoresize />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import type { WorkloadItem } from '@/api/statistics'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent])

const props = defineProps<{ data: WorkloadItem[] }>()

const option = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 16, right: 16, top: 8, bottom: 8, containLabel: true },
  xAxis: { type: 'value', minInterval: 1 },
  yAxis: { type: 'category', data: props.data.map(i => i.name).reverse() },
  series: [{
    type: 'bar',
    data: props.data.map(i => i.pendingCount).reverse(),
    itemStyle: { borderRadius: [0, 8, 8, 0] },
  }],
}))
</script>

<script lang="ts">
export default { name: 'WorkloadChart' }
</script>

<style scoped>
.chart { height: 260px; width: 100%; }
</style>
