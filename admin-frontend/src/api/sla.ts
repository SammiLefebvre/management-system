import request from './request'

const BASE = '/api/sla-config'

export function getSlaConfigList() {
  return request.get(BASE)
}

export function updateSlaConfig(id: number, data: any) {
  return request.put(`${BASE}/${id}`, data)
}
