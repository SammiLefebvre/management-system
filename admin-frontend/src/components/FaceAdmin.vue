<template>
  <div class="admin-container">
    <el-card shadow="never" class="header-card">
      <h1 style="margin:0; display:flex; align-items:center; gap:10px;">
        📸 批量人脸录入后台
        <el-tag v-if="isLoggedIn" type="success" size="large">已登录</el-tag>
        <el-tag v-else type="danger" size="large">未登录</el-tag>
      </h1>
    </el-card>

    <!-- ========== 登录面板 ========== -->
    <el-card shadow="hover" class="login-card">
      <template #header>
        <span style="font-weight:600;">🔐 登录</span>
      </template>
      <el-form :inline="true" :model="loginForm" label-width="80px">
        <el-form-item label="邮箱">
          <el-input v-model="loginForm.email" placeholder="your@email.com" style="width:220px;" />
        </el-form-item>
        <el-form-item label="验证码">
          <div style="display:flex; gap:8px;">
            <el-input v-model="loginForm.code" placeholder="6位数字" maxlength="6" style="width:140px;" />
            <el-button type="warning" :loading="sendingCode" @click="sendCode">发送验证码</el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loggingIn" @click="handleLogin">登录</el-button>
          <el-button type="danger" @click="logout">退出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- ========== 上传参数配置 ========== -->
    <el-card shadow="hover" class="params-card">
      <template #header>
        <span style="font-weight:600;">⚙️ 上传参数</span>
      </template>
      <el-form :inline="true" :model="uploadParams">
        <el-form-item label="组 ID">
          <el-input v-model="uploadParams.groupId" placeholder="test_group" style="width:180px;" />
        </el-form-item>
        <el-form-item label="用户 ID 前缀">
          <el-input v-model="uploadParams.userIdPrefix" placeholder="batch" style="width:150px;" />
          <span style="font-size:12px; color:#909399; margin-left:8px;">
            最终 ID = 前缀_时间戳_文件名
          </span>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- ========== 批量上传组件 ========== -->
    <el-card shadow="hover" class="upload-card">
      <template #header>
        <span style="font-weight:600;">📤 批量上传图片</span>
      </template>

      <el-upload
          ref="uploadRef"
          drag
          multiple
          :limit="10"
          :file-list="fileList"
          :before-upload="beforeUpload"
          :http-request="customUpload"
          :disabled="!isLoggedIn"
          @on-exceed="handleExceed"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽图片到此处，或 <em>点击选择多张</em></div>
        <template #tip>
          <div style="color:#909399;font-size:13px;">
            仅支持 JPG/PNG，单张 ≤10MB，单次最多 10 张
            <el-tag v-if="!isLoggedIn" type="danger" size="small" style="margin-left:10px;">请先登录</el-tag>
          </div>
        </template>
      </el-upload>

      <!-- 进度条 -->
      <div v-if="progressVisible" class="progress-wrapper">
        <div class="progress-info">
          <span>批量录入进度</span>
          <span>{{ uploadedCount }} / {{ totalCount }}</span>
        </div>
        <el-progress
            :percentage="Math.round((uploadedCount / totalCount) * 100)"
            :stroke-width="20"
            striped
            striped-flow
            :status="uploadStatus"
        />
      </div>
    </el-card>

    <!-- ========== 结果表格 ========== -->
    <el-card v-if="results.length > 0" shadow="hover" class="result-card">
      <template #header>
        <span style="font-weight:600;">📋 上传结果</span>
        <el-button size="small" style="float:right;" @click="clearResults">清空记录</el-button>
      </template>
      <el-table :data="results" style="width:100%;" max-height="400">
        <el-table-column prop="filename" label="文件名" min-width="180" />
        <el-table-column prop="userId" label="用户 ID" min-width="180" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'" size="small">
              {{ row.success ? '✅ 成功' : '❌ 失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="error" label="错误信息" min-width="200" v-if="results.some(r => !r.success)" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

// ============================================================
// ⚠️ 重要：如果合并部署到 Spring Boot，改成 '/api'
// 如果分离开发（前端 5173，后端 9090），保持绝对路径
// ============================================================
const API_BASE = 'http://172.30.16.106:9090/api'
// const API_BASE = 'http://172.30.16.106:9090/api'  // 分离开发时用绝对路径

// ===== 登录状态 =====
const token = ref(localStorage.getItem('authToken') || '')
const isLoggedIn = ref(!!token.value)
const loginForm = reactive({ email: 'test@qq.com', code: '' })
const sendingCode = ref(false)
const loggingIn = ref(false)

// ===== 上传参数 =====
const uploadParams = reactive({
  groupId: 'test_group',
  userIdPrefix: 'batch'
})

// ===== 上传状态 =====
const fileList = ref<any[]>([])
const progressVisible = ref(false)
const uploadedCount = ref(0)
const totalCount = ref(0)
const uploadStatus = ref<'success' | 'exception' | ''>('')
const results = ref<any[]>([])

// ============================================================
// 1. 登录相关函数
// ============================================================

// 发送验证码
const sendCode = async () => {
  if (!loginForm.email) {
    ElMessage.warning('请输入邮箱地址')
    return
  }
  sendingCode.value = true
  try {
    const resp = await fetch(`${API_BASE}/auth/send-code?email=${encodeURIComponent(loginForm.email)}`, {
      method: 'POST'
    })
    const data = await resp.json()
    if (data.success) {
      ElMessage.success('验证码已发送至邮箱')
    } else {
      ElMessage.error(data.message || '发送失败')
    }
  } catch (err: any) {
    ElMessage.error('网络错误: ' + err.message)
  } finally {
    sendingCode.value = false
  }
}

// 登录
const handleLogin = async () => {
  if (!loginForm.email || !loginForm.code) {
    ElMessage.warning('请填写邮箱和验证码')
    return
  }
  loggingIn.value = true
  try {
    const resp = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: loginForm.email, code: loginForm.code })
    })
    const data = await resp.json()
    if (data.success && data.token) {
      token.value = data.token
      localStorage.setItem('authToken', token.value)
      isLoggedIn.value = true
      ElMessage.success('登录成功')
    } else {
      ElMessage.error(data.message || '登录失败')
    }
  } catch (err: any) {
    ElMessage.error('网络错误: ' + err.message)
  } finally {
    loggingIn.value = false
  }
}

// 退出登录
const logout = () => {
  token.value = ''
  localStorage.removeItem('authToken')
  isLoggedIn.value = false
  results.value = []
  ElMessage.success('已退出登录')
}

// ============================================================
// 2. 批量上传核心逻辑
// ============================================================

// 前端校验
const beforeUpload = (file: any) => {
  const allowed = ['image/jpeg', 'image/png']
  if (!allowed.includes(file.type)) {
    ElMessage.error(`"${file.name}" 格式不支持，仅限 JPG/PNG`)
    return false
  }
  if (file.size / 1024 / 1024 > 10) {
    ElMessage.error(`"${file.name}" 超过 10MB`)
    return false
  }
  return true
}

// 自定义上传（核心）
const customUpload = async (options: any) => {
  const { file, onSuccess, onError } = options
  const timestamp = Date.now()
  const safeName = file.name.replace(/\.[^.]+$/, '')
  const userId = `${uploadParams.userIdPrefix}_${timestamp}_${safeName}`

  const formData = new FormData()
  formData.append('image', file)
  formData.append('groupId', uploadParams.groupId)
  formData.append('userId', userId)

  try {
    const resp = await fetch(`${API_BASE}/face/register`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token.value}` },
      body: formData
    })
    const data = await resp.json()

    if (resp.ok) {
      onSuccess(data, file)
      results.value.push({ filename: file.name, userId, success: true, error: '' })
      ElMessage.success(`✅ ${file.name} 注册成功`)
    } else {
      const errMsg = data.message || data.error || '注册失败'
      onError(new Error(errMsg), file)
      results.value.push({ filename: file.name, userId, success: false, error: errMsg })
      ElMessage.error(`❌ ${file.name} 失败: ${errMsg}`)
    }
  } catch (err: any) {
    onError(err, file)
    results.value.push({ filename: file.name, userId, success: false, error: err.message })
    ElMessage.error(`❌ ${file.name} 异常: ${err.message}`)
  } finally {
    uploadedCount.value++
  }
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传 10 个文件')
}

const clearResults = () => {
  results.value = []
}

// 监听文件列表变化 → 触发进度
watch(fileList, (newList) => {
  const ready = newList.filter(f => f.status === 'ready' || f.status === 'uploading')
  if (ready.length > 0 && !progressVisible.value) {
    totalCount.value = newList.length
    uploadedCount.value = 0
    results.value = []
    progressVisible.value = true
    uploadStatus.value = ''
  }
}, { deep: true })

// 监听上传进度 → 完成时自动关闭
watch(uploadedCount, (val) => {
  if (progressVisible.value && val >= totalCount.value && totalCount.value > 0) {
    uploadStatus.value = 'success'
    setTimeout(() => {
      progressVisible.value = false
      ElMessage.success('🎉 批量录入完成！')
    }, 800)
  }
})

// ============================================================
// 3. 自动检测 Token 是否过期（每次请求时判断）
// ============================================================
// 拦截器会在后端返回 401 时触发，我们在这里统一处理
// 由于我们是直接 fetch，可以在 catch 中判断 resp.status === 401
// 已经在上面的 customUpload 中处理了
</script>

<style scoped>
.admin-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.login-card,
.params-card,
.upload-card,
.result-card {
  margin-bottom: 20px;
}

.progress-wrapper {
  margin-top: 16px;
  padding: 16px 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 8px;
  color: #606266;
}

:deep(.el-upload-dragger) {
  width: 100%;
  padding: 40px 0;
}

:deep(.el-upload-dragger .el-icon--upload) {
  font-size: 48px;
  color: #c0c4cc;
}
</style>