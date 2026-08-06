import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi, sendCodeApi } from '@/api/auth'
import { faceLoginApi } from '@/api/face'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  async function sendCode(email: string) {
    await sendCodeApi(email)
    ElMessage.success('验证码已发送')
  }

  async function login(email: string, code: string) {
    const res = await loginApi({ email, code })
    token.value = res.data.token
    userInfo.value = {
      userId: res.data.userId,
      account: res.data.account,
      role: res.data.role,
      projectGroup: res.data.projectGroup
    }
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    ElMessage.success('登录成功')
  }

  async function faceLogin(imageBase64: string) {
    const res = await faceLoginApi({ imageBase64 })
    token.value = res.data.token
    userInfo.value = {
      userId: res.data.userId,
      account: res.data.account,
      role: res.data.role,
      projectGroup: res.data.projectGroup
    }
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    ElMessage.success('人脸登录成功')
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, sendCode, login, faceLogin, logout }
})
