import request from './request'

export interface DashboardStatistics {
  workOrderTotal: number
  workOrderToday: number
  statusCounts: Record<string, number>
  slaOverdueCount: number
  deviceTotal: number
  personnelTotal: number
  teamTotal: number
  last7Days: Array<{ date: string; created: number; completed: number }>
}

export interface WorkOrderTrend {
  dates: string[]
  series: Array<{ name: string; data: number[] }>
}

export interface HeatmapData {
  xAxis: string[]
  yAxis: string[]
  data: number[][]
}

export interface WorkloadItem {
  personnelId: number
  name: string
  role: string
  pendingCount: number
  completedThisWeek: number
  avgResponseMinutes: number
}

export interface DeviceMapItem {
  id: number
  deviceName: string
  deviceCode: string
  latitude: number
  longitude: number
  area: string
  projectGroup: string
  latestWorkOrderStatus: string | null
}

export function getDashboardStatistics(): Promise<{ data: DashboardStatistics }> {
  return request.get('/api/statistics/dashboard') as Promise<{ data: DashboardStatistics }>
}

export function getTrends(days = 30): Promise<{ data: WorkOrderTrend }> {
  return request.get(`/api/statistics/trends?days=${days}`) as Promise<{ data: WorkOrderTrend }>
}

export function getHeatmap(): Promise<{ data: HeatmapData }> {
  return request.get('/api/statistics/heatmap') as Promise<{ data: HeatmapData }>
}

export function getWorkload(): Promise<{ data: WorkloadItem[] }> {
  return request.get('/api/statistics/workload') as Promise<{ data: WorkloadItem[] }>
}

export function getDevicesWithLocation(): Promise<{ data: DeviceMapItem[] }> {
  return request.get('/api/statistics/devices-with-location') as Promise<{ data: DeviceMapItem[] }>
}
