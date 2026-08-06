<template>
  <div class="map-board">
    <div v-if="error" class="map-error">
      <el-empty :description="error" />
    </div>
    <div v-else ref="mapRef" class="map-container" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useAMapLoader } from '@/composables/useAMap'
import type { DeviceMapItem } from '@/api/statistics'

const props = defineProps<{ devices: DeviceMapItem[] }>()
const mapRef = ref<HTMLDivElement>()
const error = ref('')
let map: any = null
let markers: any[] = []
const statusColor: Record<string, string> = {
  published: '#0071e3',
  claimed: '#ff9500',
  in_progress: '#ff9500',
  completing: '#ff9500',
  pending_confirm: '#af52de',
  confirmed: '#34c759',
  closed: '#34c759',
}

function render() {
  if (!map || !props.devices.length) return
  markers.forEach(m => map.remove(m))
  markers = []
  const AMap = window.AMap
  props.devices.forEach(d => {
    const marker = new AMap.Marker({
      position: [d.longitude, d.latitude],
      title: d.deviceName,
      icon: new AMap.Icon({
        size: new AMap.Size(24, 24),
        image: statusIcon(d.latestWorkOrderStatus),
        imageSize: new AMap.Size(24, 24),
      }),
    })
    marker.on('click', () => {
      const info = new AMap.InfoWindow({
        content: `<div style="padding:8px"><b>${d.deviceName}</b><br/>状态：${d.latestWorkOrderStatus || '正常'}</div>`,
        offset: new AMap.Pixel(0, -12),
      })
      info.open(map, marker.getPosition())
    })
    map.add(marker)
    markers.push(marker)
  })
  if (markers.length) map.setFitView()
}

function statusIcon(status: string | null) {
  const color = statusColor[status || ''] || '#34c759'
  const svg = encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" fill="${color}"/></svg>`)
  return `data:image/svg+xml;utf8,${svg}`
}

onMounted(async () => {
  try {
    const AMap = await useAMapLoader().load()
    map = new AMap.Map(mapRef.value, { zoom: 11, viewMode: '2D' })
    render()
  } catch (e: any) {
    error.value = e.message || '地图加载失败'
  }
})

watch(() => props.devices, render, { deep: true })
</script>

<script lang="ts">
export default { name: 'MapBoard' }
</script>

<style scoped>
.map-board { width: 100%; height: 100%; position: relative; }
.map-container { width: 100%; height: 100%; border-radius: 24px; }
.map-error { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
</style>
