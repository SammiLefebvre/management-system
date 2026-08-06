import request from './request'

const BASE = '/api/code-table'

export function getCodeTable(type: string) {
  return request.get(`${BASE}/${type}`)
}

export function addCodeValue(data: any) {
  return request.post(BASE, data)
}

export function updateCodeValue(data: any) {
  return request.put(BASE, data)
}

export function deleteCodeValue(id: number) {
  return request.delete(`${BASE}/${id}`)
}
