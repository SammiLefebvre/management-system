import request from './request'

export interface LoginResponse {
  success: boolean
  token: string
  message: string
  userId: number
  account: string
  role: string
  projectGroup: string
}

export function faceLoginApi(data: { imageBase64: string }) {
  return request.post<LoginResponse>('/api/auth/face-login', data)
}
