<template>
  <div class="mini-trend">
    <div
      v-for="(item, idx) in data"
      :key="idx"
      class="mini-trend-bar"
      :style="barStyle(item)"
      :title="`${item.date}: 新建${item.created}, 完成${item.completed}`"
    />
  </div>
</template>

<script setup lang="ts">
interface TrendItem {
  date: string
  created: number
  completed: number
}
const props = defineProps<{ data: TrendItem[] }>()

function barStyle(item: TrendItem) {
  const max = Math.max(...props.data.map(d => Math.max(d.created, d.completed || 0)), 1)
  const height = `${(item.created / max) * 100}%`
  return { height }
}
</script>

<script lang="ts">
export default { name: 'MiniTrend' }
</script>

<style scoped>
.mini-trend {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 6px;
  height: 64px;
}
.mini-trend-bar {
  flex: 1;
  background: linear-gradient(to top, var(--accent), rgba(0,113,227,0.3));
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  transition: height 0.8s cubic-bezier(0.25, 0.1, 0.25, 1.0);
}
</style>
