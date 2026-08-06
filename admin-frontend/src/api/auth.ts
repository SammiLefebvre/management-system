import request from './request'

/** 发送验证码 */
export function sendCodeApi(email: string) {
  return request.post('/api/auth/send-code', null, { params: { email } })
}

/** 登录 */
export function loginApi(data: { email: string; code: string }) {
  return request.post('/api/auth/login', data)
}
