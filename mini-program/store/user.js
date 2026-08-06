import { reactive, toRefs } from 'vue'

const state = reactive({
  token: uni.getStorageSync('token') || '',
  userInfo: uni.getStorageSync('userInfo') || {}
})

export function useUserStore() {
  function setToken(token) {
    state.token = token
    uni.setStorageSync('token', token)
  }

  function setUserInfo(info) {
    state.userInfo = info
    uni.setStorageSync('userInfo', info)
  }

  function logout() {
    state.token = ''
    state.userInfo = {}
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
  }

  return {
    ...toRefs(state),
    setToken,
    setUserInfo,
    logout
  }
}
