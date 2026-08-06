<template>
  <div class="login-page">
    <div class="login-bg" />
    <div class="login-card-wrapper">
      <div class="login-card">
        <div class="login-icon">
          <el-icon size="32" color="#fff"><Memo /></el-icon>
        </div>
        <h1 class="login-title">工单管理系统</h1>
        <p class="login-subtitle">欢迎回来，请登录您的账号</p>

        <el-tabs v-model="activeTab" class="login-tabs">
          <el-tab-pane label="验证码登录" name="code">
            <el-form :model="form" class="login-form">
              <el-form-item>
                <el-input
                  v-model="form.email"
                  placeholder="请输入邮箱账号"
                  size="large"
                  class="login-input"
                />
              </el-form-item>
              <el-form-item>
                <div class="code-row">
                  <el-input
                    v-model="form.code"
                    placeholder="6位验证码"
                    maxlength="6"
                    size="large"
                    class="login-input"
                  />
                  <el-button
                    type="primary"
                    size="large"
                    round
                    :loading="sending"
                    @click="handleSendCode"
                  >
                    发送验证码
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  round
                  class="login-submit"
                  :loading="loading"
                  @click="handleLogin"
                >
                  登 录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="人脸登录" name="face">
            <div class="face-login">
              <FaceCapture @capture="handleFaceCapture" />
            </div>
          </el-tab-pane>
        </el-tabs>

        <p class="login-hint">演示环境验证码可输入任意 6 位数字</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { Memo } from '@element-plus/icons-vue'
import FaceCapture from '@/components/FaceCapture.vue'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('code')
const form = reactive({ email: '', code: '' })
const sending = ref(false)
const loading = ref(false)

async function handleSendCode() {
  if (!form.email) {
    return ElMessage.warning('请输入邮箱')
  }
  sending.value = true
  try {
    await userStore.sendCode(form.email)
  } finally {
    sending.value = false
  }
}

async function handleLogin() {
  if (!form.email || !form.code) {
    return ElMessage.warning('请填写完整信息')
  }
  loading.value = true
  try {
    await userStore.login(form.email, form.code)
    router.push('/')
  } finally {
    loading.value = false
  }
}

async function handleFaceCapture(imageBase64: string) {
  loading.value = true
  try {
    await userStore.faceLogin(imageBase64)
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<script lang="ts">
export default { name: 'LoginPage' }
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background-color: var(--bg-primary);
}
.login-bg {
  position: absolute;
  inset: -50%;
  background:
    radial-gradient(circle at 20% 30%, rgba(0,113,227,0.18), transparent 40%),
    radial-gradient(circle at 80% 70%, rgba(175,82,222,0.15), transparent 40%),
    radial-gradient(circle at 50% 50%, rgba(0,113,227,0.08), transparent 50%);
  animation: gradient-flow 20s ease infinite;
  background-size: 200% 200%;
  z-index: 0;
}
.login-card-wrapper {
  position: relative;
  z-index: 1;
  animation: fade-in-up 0.6s cubic-bezier(0.25, 0.1, 0.25, 1.0) both;
}
.login-card {
  width: 420px;
  padding: 48px 40px;
  border-radius: 28px;
  background: rgba(255,255,255,0.82);
  backdrop-filter: blur(24px);
  border: 1px solid rgba(255,255,255,0.3);
  box-shadow: var(--shadow-lg);
  text-align: center;
}
.dark .login-card {
  background: rgba(30,30,30,0.85);
  border-color: rgba(255,255,255,0.08);
}
.login-icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: linear-gradient(135deg, var(--accent), #2997ff);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 8px 24px rgba(0,113,227,0.3);
}
.login-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: -0.02em;
  color: var(--text-primary);
}
.login-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0 0 24px;
}
.login-tabs :deep(.el-tabs__nav-wrap::after) {
  background: transparent;
}
.login-tabs :deep(.el-tabs__item) {
  color: var(--text-secondary);
}
.login-tabs :deep(.el-tabs__item.is-active) {
  color: var(--accent);
}
.login-tabs :deep(.el-tabs__active-bar) {
  background: var(--accent);
}
.login-form {
  text-align: left;
  margin-top: 8px;
}
.face-login {
  margin-top: 16px;
}
.code-row {
  display: flex;
  gap: 12px;
  width: 100%;
}
.code-row .el-input {
  flex: 1;
}
.login-submit {
  width: 100%;
  margin-top: 8px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--accent), #2997ff);
  border: none;
  transition: transform 0.15s ease, box-shadow 0.2s ease;
}
.login-submit:hover {
  box-shadow: 0 6px 20px rgba(0,113,227,0.35);
}
.login-submit:active {
  transform: scale(0.98);
}
.login-hint {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-top: 24px;
}
</style>
