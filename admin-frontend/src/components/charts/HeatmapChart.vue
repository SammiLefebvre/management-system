<template>
  <v-chart class="chart" :option="option" autoresize />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { HeatmapChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, VisualMapComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import type { HeatmapData } from '@/api/statistics'

use([CanvasRenderer, HeatmapChart, GridComponent, TooltipComponent, VisualMapComponent])

const props = defineProps<{ data: HeatmapData }>()

const option = computed(() => ({
  tooltip: { position: 'top' },
  grid: { left: 16, right: 16, top: 8, bottom: 64, containLabel: true },
  xAxis: { type: 'category', data: props.data.xAxis, splitArea: { show: true } },
  yAxis: { type: 'category', data: props.data.yAxis, splitArea: { show: true } },
  visualMap: { min: 0, max: 20, calculable: true, orient: 'horizontal', left: 'center', bottom: 0 },
  series: [{
    type: 'heatmap',
    data: props.data.data,
    label: { show: true },
    emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.5)' } },
  }],
}))
</script>

<script lang="ts">
export default { name: 'HeatmapChart' }
</script>

<style scoped>
.chart { height: 320px; width: 100%; }
</style>
