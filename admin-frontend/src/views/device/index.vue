<template>
  <div class="device-page">
    <AppCard class="page-card">
      <template #header>
        <div class="page-header">
          <span>设备台账</span>
          <div class="page-actions">
            <el-button type="primary" round @click="showAddDialog = true">新增</el-button>
            <el-upload :action="importUrl" :headers="uploadHeaders" :show-file-list="false"
              :on-success="handleImportSuccess" accept=".xlsx" style="display:inline-block;margin-left:8px">
              <el-button type="warning" round>Excel 导入</el-button>
            </el-upload>
            <el-button round @click="handleExport" style="margin-left:8px">导出 Excel</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="deviceCode" label="编号" width="120" />
        <el-table-column prop="deviceName" label="名称" width="150" />
        <el-table-column prop="area" label="区域" width="100" />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="cameraType" label="摄像机类型" width="110" />
        <el-table-column prop="operationType" label="运营类型" width="110" />
        <el-table-column prop="projectGroup" label="项目组" width="110" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" round @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" round @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total"
          layout="total, prev, pager, next" @current-change="fetchData" />
      </div>
    </AppCard>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showAddDialog" :title="editingId ? '编辑设备' : '新增设备'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="编号"><el-input v-model="form.deviceCode" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.deviceName" /></el-form-item>
        <el-form-item label="区域"><el-input v-model="form.area" /></el-form-item>
        <el-form-item label="Mac"><el-input v-model="form.mac" /></el-form-item>
        <el-form-item label="IP"><el-input v-model="form.ip" /></el-form-item>
        <el-form-item label="经度"><el-input v-model="form.longitude" /></el-form-item>
        <el-form-item label="纬度"><el-input v-model="form.latitude" /></el-form-item>
        <el-form-item label="摄像机类型"><el-input v-model="form.cameraType" /></el-form-item>
        <el-form-item label="运营类型"><el-input v-model="form.operationType" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" round @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import AppCard from '@/components/AppCard.vue'
import { ref, reactive, computed, onMounted } from 'vue'
import { getDevicePage, addDevice, updateDevice, deleteDevice } from '@/api/device'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const showAddDialog = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({
  deviceCode: '', deviceName: '', area: '', mac: '', ip: '',
  longitude: '', latitude: '', cameraType: '', operationType: ''
})

const uploadHeaders = computed(() => ({ Authorization: `Bearer ${localStorage.getItem('token')}` }))
const importUrl = 'http://localhost:9090/api/device/import'

async function fetchData() {
  loading.value = true
  try {
    const res = await getDevicePage({ page: page.value, size: size.value })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleEdit(row: any) {
  editingId.value = row.id
  Object.assign(form, {
    deviceCode: row.deviceCode, deviceName: row.deviceName, area: row.area || '',
    mac: row.mac || '', ip: row.ip || '', longitude: row.longitude || '',
    latitude: row.latitude || '', cameraType: row.cameraType || '', operationType: row.operationType || ''
  })
  showAddDialog.value = true
}

async function handleSave() {
  if (editingId.value) {
    await updateDevice({ ...form, id: editingId.value })
    ElMessage.success('修改成功')
  } else {
    await addDevice(form)
    ElMessage.success('新增成功')
  }
  showAddDialog.value = false
  editingId.value = null
  Object.keys(form).forEach(k => (form as any)[k] = '')
  fetchData()
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    await deleteDevice(id)
    ElMessage.success('已删除')
    fetchData()
  } catch { /* 取消 */ }
}

function handleImportSuccess(res: any) {
  ElMessage.success(res.data || '导入成功')
  fetchData()
}

function handleExport() {
  window.open('http://localhost:9090/api/device/export', '_blank')
}

onMounted(fetchData)
</script>

<style scoped>
.device-page {
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
.page-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>
