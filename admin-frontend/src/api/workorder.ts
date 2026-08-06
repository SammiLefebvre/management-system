import request from './request'

const BASE = '/api/work-order'

export function getWorkOrderPage(params: any) {
  return request.get(`${BASE}/page`, { params })
}

export function getWorkOrderDetail(id: number) {
  return request.get(`${BASE}/${id}`)
}

export function getWorkOrderLogs(id: number) {
  return request.get(`${BASE}/${id}/logs`)
}

export function createWorkOrder(data: any) {
  return request.post(BASE, data)
}

export function publishWorkOrder(id: number) {
  return request.put(`${BASE}/${id}/publish`)
}

export function claimWorkOrder(id: number) {
  return request.put(`${BASE}/${id}/claim`)
}

export function cancelClaimWorkOrder(id: number) {
  return request.put(`${BASE}/${id}/cancel-claim`)
}

export function checkinWorkOrder(id: number, data: any) {
  return request.put(`${BASE}/${id}/checkin`, data)
}

export function submitProcessWorkOrder(id: number, data: any) {
  return request.put(`${BASE}/${id}/process`, data)
}

export function submitCompleteWorkOrder(id: number, data: any) {
  return request.put(`${BASE}/${id}/complete`, data)
}

export function confirmWorkOrder(id: number) {
  return request.put(`${BASE}/${id}/confirm`)
}

export function forceCloseWorkOrder(id: number, reason: string) {
  return request.put(`${BASE}/${id}/force-close`, null, { params: { reason } })
}

export function confirmForceCloseWorkOrder(id: number) {
  return request.put(`${BASE}/${id}/confirm-force-close`)
}

export function togglePriorityWorkOrder(id: number) {
  return request.put(`${BASE}/${id}/toggle-priority`)
}
