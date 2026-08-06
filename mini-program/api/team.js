import http from './request.js'

const BASE = '/api/team'

export function getTeamList(params) {
  return http.get(`${BASE}/list`, params)
}

export function getTeamDetail(id) {
  return http.get(`${BASE}/${id}`)
}

export function createTeam(data) {
  return http.post(BASE, data)
}

export function updateTeam(data) {
  return http.put(BASE, data)
}

export function deleteTeam(id) {
  return http.delete(`${BASE}/${id}`)
}

export function getExternalPersonnel() {
  return http.get('/api/personnel/external')
}
