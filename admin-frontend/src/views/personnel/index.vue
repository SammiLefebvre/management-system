<template>
  <div class="personnel-page">
    <AppCard class="page-card">
      <template #header>
        <div class="page-header">
          <span>人员管理</span>
          <el-button type="primary" round @click="showDialog = true">新增人员</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="account" label="账号" width="180" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="role" label="角色" width="130">
          <template #default="{ row }">
            <StatusTag type="info" :label="row.role" />
          </template>
        </el-table-column>
        <el-table-column prop="projectGroup" label="项目组" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :type="row.status === 1 ? 'success' : 'danger'" :label="row.status === 1 ? '启用' : '禁用'" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" round @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" round @click="handleRegisterFace(row)">人脸录入</el-button>
            <el-button size="small" type="danger" round @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="page" :total="total" layout="total, prev, pager, next" @current-change="fetchData" />
      </div>
    </AppCard>

    <el-dialog v-model="showDialog" :title="editingId ? '编辑人员' : '新增人员'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号"><el-input v-model="form.account" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width:100%">
            <el-option v-for="item in roles" :key="item.codeValue" :label="item.codeLabel" :value="item.codeValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目组">
          <el-select v-model="form.projectGroup" placeholder="请选择或输入" style="width:100%" filterable allow-create>
            <el-option v-for="item in projectGroups" :key="item.codeValue" :label="item.codeLabel" :value="item.codeValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="showDialog = false">取消</el-button>
        <el-button type="primary" round @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="showFaceDialog" title="人脸录入" width="420px" :close-on-click-modal="false">
      <div class="face-register-body">
        <p class="face-register-tip">正在为 <strong>{{ faceTarget?.name }}</strong>（{{ faceTarget?.account }}）录入人脸</p>
        <FaceCapture @capture="onFaceCaptured" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getPersonnelPage, addPersonnel, updatePersonnel, deletePersonnel } from '@/api/personnel'
import { registerFace } from '@/api/ai'
import FaceCapture from '@/components/FaceCapture.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppCard from '@/components/AppCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useCodeTable, type CodeItem } from '@/composables/useCodeTable'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = ref(1)

const showDialog = ref(false)
const showFaceDialog = ref(false)
const faceRegistering = ref(false)
const faceTarget = ref<any>(null)
const editingId = ref<number | null>(null)
const form = reactive({ account: '', name: '', phone: '', role: '外场', projectGroup: '', status: 1 })
const { load: loadCodeTable, getCached } = useCodeTable()
const roles = computed<CodeItem[]>(() => getCached('role'))
const projectGroups = computed<CodeItem[]>(() => getCached('project_group'))

async function fetchData() {
  loading.value = true
  try {
    const res = await getPersonnelPage({ page: page.value, size: 10 })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleEdit(row: any) {
  editingId.value = row.id
  Object.assign(form, {
    account: row.account, name: row.name, phone: row.phone,
    role: row.role, projectGroup: row.projectGroup, status: row.status
  })
  showDialog.value = true
}

async function handleSave() {
  if (editingId.value) {
    await updatePersonnel({ ...form, id: editingId.value })
    ElMessage.success('修改成功')
  } else {
    await addPersonnel(form)
    ElMessage.success('新增成功')
  }
  showDialog.value = false
  editingId.value = null
  Object.keys(form).forEach(k => (form as any)[k] = '')
  fetchData()
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    await deletePersonnel(id)
    ElMessage.success('已删除')
    fetchData()
  } catch { /* 取消 */ }
}

function handleRegisterFace(row: any) {
  faceTarget.value = row
  showFaceDialog.value = true
}

async function onFaceCaptured(imageBase64: string) {
  if (!faceTarget.value) return
  faceRegistering.value = true
  try {
    await registerFace(imageBase64, faceTarget.value.account)
    ElMessage.success('人脸录入成功')
    showFaceDialog.value = false
  } finally {
    faceRegistering.value = false
  }
}

onMounted(async () => {
  await Promise.all([
    loadCodeTable('role'),
    loadCodeTable('project_group')
  ])
  fetchData()
})
</script>

<script lang="ts">
export default { name: 'PersonnelPage' }
</script>

<style scoped>
.personnel-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.page-card {
  flex: 1;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 18px;
}
.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
.face-register-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
.face-register-tip {
  font-size: 14px;
  color: var(--text-secondary);
  text-align: center;
}
</style>
