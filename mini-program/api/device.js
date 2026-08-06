import http from './request.js'

export function getDeviceListByType(operationType) {
  return http.get('/api/device/list-by-type', { operationType })
}

export function getCodeTable(type) {
  return http.get(`/api/code-table/${type}`)
}
