import request from './request'

const BASE = '/api/device'

export function getDevicePage(params: any) {
  return request.get(`${BASE}/page`, { params })
}

export function getDeviceListByType(operationType?: string) {
  return request.get(`${BASE}/list-by-type`, { params: { operationType } })
}

export function getDeviceDetail(id: number) {
  return request.get(`${BASE}/${id}`)
}

export function addDevice(data: any) {
  return request.post(BASE, data)
}

export function updateDevice(data: any) {
  return request.put(BASE, data)
}

export function deleteDevice(id: number) {
  return request.delete(`${BASE}/${id}`)
}

export function importDeviceExcel(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`${BASE}/import`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
