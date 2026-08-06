import request from './request'
import { getHuggingFaceToken } from '@/composables/useHuggingFaceToken'

function hfHeaders() {
  const token = getHuggingFaceToken()
  return token ? { 'X-HF-Token': token } : {}
}

export function chat(message: string) {
  return request.post<string>('/api/ai/chat', { message }, { headers: hfHeaders() })
}

export interface DispatchAdvice {
  workOrderId: number
  personnelId: number
  name: string
  reason: string
}

export function dispatchAdvice(workOrderId: number) {
  return request.post<DispatchAdvice>(`/api/ai/dispatch/advice?workOrderId=${workOrderId}`, {}, { headers: hfHeaders() })
}

export function assignWorkOrder(id: number, personnelId: number) {
  return request.put(`/api/work-order/${id}/assign?personnelId=${personnelId}`)
}

export function registerFace(imageBase64: string, userId: string) {
  return request.post('/api/face/register/base64', { image: imageBase64, groupId: 'gzgd_users', userId })
}
