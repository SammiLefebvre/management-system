import http from './request.js'

const BASE = '/api/work-order'

export function getWorkOrderList(params) {
  return http.get(`${BASE}/page`, params)
}

export function getWorkOrderDetail(id) {
  return http.get(`${BASE}/${id}`)
}

export function getWorkOrderLogs(id) {
  return http.get(`${BASE}/${id}/logs`)
}

export function claimWorkOrder(id) {
  return http.put(`${BASE}/${id}/claim`)
}

export function cancelClaimWorkOrder(id) {
  return http.put(`${BASE}/${id}/cancel-claim`)
}

export function checkinWorkOrder(id, data) {
  return http.put(`${BASE}/${id}/checkin`, data)
}

export function submitProcess(id, data) {
  return http.put(`${BASE}/${id}/process`, data)
}

export function submitComplete(id, data) {
  return http.put(`${BASE}/${id}/complete`, data)
}

export function togglePriority(id) {
  return http.put(`${BASE}/${id}/toggle-priority`)
}

export function createWorkOrder(data) {
  return http.post(BASE, data)
}

export function publishWorkOrder(id) {
  return http.put(`${BASE}/${id}/publish`)
}
