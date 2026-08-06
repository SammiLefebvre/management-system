import request from './request'

const BASE = '/api/team'

export function getTeamList(params?: any) {
  return request.get(`${BASE}/list`, { params })
}

export function getTeamDetail(id: number) {
  return request.get(`${BASE}/${id}`)
}

export function getTeamMembers(id: number) {
  return request.get(`${BASE}/${id}/members`)
}

export function getTeamVehicles(id: number) {
  return request.get(`${BASE}/${id}/vehicles`)
}

export function createTeam(data: any) {
  return request.post(BASE, data)
}

export function updateTeam(data: any) {
  return request.put(BASE, data)
}

export function deleteTeam(id: number) {
  return request.delete(`${BASE}/${id}`)
}
