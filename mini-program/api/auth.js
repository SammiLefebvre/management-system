import http from './request.js'

export function wxLogin(code) {
  return http.get('/api/auth/wx-login', { code })
}

export function getUserInfo() {
  return uni.getStorageSync('userInfo') || {}
}

export function setUserInfo(info) {
  uni.setStorageSync('userInfo', info)
}
