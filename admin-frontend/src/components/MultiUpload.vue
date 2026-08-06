<template>
  <div class="upload-container">
    <el-card shadow="never" style="margin-bottom:20px;">
      <el-form :inline="true" :model="params">
        <el-form-item label="组 ID">
          <el-input v-model="params.groupId" placeholder="test_group" style="width:180px;" />
        </el-form-item>
        <el-form-item label="用户 ID 前缀">
          <el-input v-model="params.userIdPrefix" placeholder="batch" style="width:150px;" />
        </el-form-item>
        <el-form-item>
          <el-tag type="info">Token 自动从 localStorage 读取</el-tag>
        </el-form-item>
      </el-form>
    </el-card>

    <el-upload
        ref="uploadRef"
        drag
        multiple
        :limit="10"
        :file-list="fileList"
        :before-upload="beforeUpload"
        :http-request="customUpload"
        @on-exceed="handleExceed"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">拖拽图片到此处，或 <em>点击选择多张</em></div>
      <template #tip>
        <div style="color:#909399;font-size:13px;">仅支持 JPG/PNG，单张 ≤10MB，单次最多 10 张</div>
      </template>
    </el-upload>

    <div v-if="progressVisible" class="progress-wrapper">
      <div class="progress-info">
        <span>批量录入进度</span>
        <span>{{ uploadedCount }} / {{ totalCount }}</span>
      </div>
      <el-progress :percentage="Math.round((uploadedCount/totalCount)*100)" :stroke-width="20" striped striped-flow />
    </div>

    <div v-if="results.length > 0" class="result-table">
      <el-table :data="results" style="width:100%;" max-height="300">
        <el-table-column prop="filename" label="文件名" min-width="180" />
        <el-table-column prop="userId" label="用户 ID" min-width="150" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'">
              {{ row.success ? '✅ 成功' : '❌ 失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="error" label="错误信息" min-width="200" v-if="results.some(r => !r.success)" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

// ===== 重要：修改这里的 IP 为你的电脑实际 IP =====
const API_BASE = 'http://172.30.16.106:9090/api'   // ⚠️ 改成你的局域网 IP
const token = localStorage.getItem('authToken') || ''

// ===== 上传参数 =====
const params = reactive({
  groupId: 'test_group',
  userIdPrefix: 'batch'
})

// ===== 状态 =====
const fileList = ref([])
const progressVisible = ref(false)
const uploadedCount = ref(0)
const totalCount = ref(0)
const results = ref([])

// ===== 前端校验 =====
const beforeUpload = (file) => {
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

// ===== 自定义上传（核心逻辑） =====
const customUpload = async (options) => {
  const { file, onSuccess, onError } = options
  const timestamp = Date.now()
  const safeName = file.name.replace(/\.[^.]+$/, '')
  const userId = `${params.userIdPrefix}_${timestamp}_${safeName}`

  const formData = new FormData()
  formData.append('image', file)
  formData.append('groupId', params.groupId)
  formData.append('userId', userId)

  try {
    const resp = await fetch(`${API_BASE}/face/register`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` },
      body: formData
    })
    const data = await resp.json()

    if (resp.ok) {
      onSuccess(data, file)
      results.value.push({ filename: file.name, userId, success: true, error: '' })
      ElMessage.success(`✅ ${file.name} 注册成功 (ID: ${userId})`)
    } else {
      const errMsg = data.message || data.error || '注册失败'
      onError(new Error(errMsg), file)
      results.value.push({ filename: file.name, userId, success: false, error: errMsg })
      ElMessage.error(`❌ ${file.name} 失败: ${errMsg}`)
    }
  } catch (err) {
    onError(err, file)
    results.value.push({ filename: file.name, userId, success: false, error: err.message })
    ElMessage.error(`❌ ${file.name} 异常: ${err.message}`)
  } finally {
    uploadedCount.value++
  }
}

const handleExceed = () => ElMessage.warning('最多只能上传 10 个文件')

// ===== 监听文件列表变化，触发进度 =====
watch(fileList, (newList) => {
  const ready = newList.filter(f => f.status === 'ready' || f.status === 'uploading')
  if (ready.length > 0 && !progressVisible.value) {
    totalCount.value = newList.length
    uploadedCount.value = 0
    results.value = []
    progressVisible.value = true
  }
}, { deep: true })

watch(uploadedCount, (val) => {
  if (progressVisible.value && val >= totalCount.value && totalCount.value > 0) {
    setTimeout(() => {
      progressVisible.value = false
      ElMessage.success('🎉 批量录入完成！')
    }, 600)
  }
})
</script>

<style scoped>
.upload-container { max-width: 900px; margin: 0 auto; padding: 20px; }
.progress-wrapper { margin-top: 16px; padding: 16px; background: #f5f7fa; border-radius: 8px; }
.progress-info { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 6px; }
.result-table { margin-top: 20px; border: 1px solid #ebeef5; border-radius: 6px; overflow: hidden; }
</style>