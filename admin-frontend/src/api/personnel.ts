import request from './request'

const BASE = '/api/personnel'

export function getPersonnelPage(params: any) {
  return request.get(`${BASE}/page`, { params })
}

export function getExternalPersonnel() {
  return request.get(`${BASE}/external`)
}

export function getPersonnelDetail(id: number) {
  return request.get(`${BASE}/${id}`)
}

export function addPersonnel(data: any) {
  return request.post(BASE, data)
}

export function updatePersonnel(data: any) {
  return request.put(BASE, data)
}

export function deletePersonnel(id: number) {
  return request.delete(`${BASE}/${id}`)
}
