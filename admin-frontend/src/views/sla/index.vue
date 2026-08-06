<template>
  <div class="sla-page">
    <AppCard class="page-card">
      <template #header>
        <span class="page-title">SLA 配置（紧急程度时限设置）</span>
      </template>

      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="emergencyLevel" label="紧急程度" width="120">
          <template #default="{ row }">
            {{ getEmergencyLabel(row.emergencyLevel) }}
          </template>
        </el-table-column>
        <el-table-column prop="targetResponseMinutes" label="目标响应时限（分钟）" width="220" />
        <el-table-column prop="targetRepairMinutes" label="目标修复时限（分钟）" width="220" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" round @click="handleEdit(row)">修改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>

    <el-dialog v-model="showDialog" title="修改 SLA 配置" width="400px">
      <el-form :model="form" label-width="160px">
        <el-form-item label="目标响应时限（分钟）">
          <el-input-number v-model="form.targetResponseMinutes" :min="1" :max="1440" />
        </el-form-item>
        <el-form-item label="目标修复时限（分钟）">
          <el-input-number v-model="form.targetRepairMinutes" :min="1" :max="10080" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="showDialog = false">取消</el-button>
        <el-button type="primary" round @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useCodeTable } from '@/composables/useCodeTable'
import { getSlaConfigList, updateSlaConfig } from '@/api/sla'
import { ElMessage } from 'element-plus'
import AppCard from '@/components/AppCard.vue'

const loading = ref(false)
const tableData = ref([])
const showDialog = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({ targetResponseMinutes: 30, targetRepairMinutes: 120 })
const { load: loadCodeTable, getEmergencyLabel, getCached } = useCodeTable()

async function fetchData() {
  loading.value = true
  try {
    const res = await getSlaConfigList()
    tableData.value = res.data || []
  } finally { loading.value = false }
}

function handleEdit(row: any) {
  editingId.value = row.id
  form.targetResponseMinutes = row.targetResponseMinutes
  form.targetRepairMinutes = row.targetRepairMinutes
  showDialog.value = true
}

async function handleSave() {
  if (editingId.value) {
    await updateSlaConfig(editingId.value, form)
    ElMessage.success('保存成功')
  }
  showDialog.value = false
  fetchData()
}

onMounted(async () => {
  await loadCodeTable('emergency_level')
  fetchData()
})

// 紧急程度码表变化时，自动重新加载 SLA 配置
watch(() => getCached('emergency_level'), () => fetchData(), { deep: true })
</script>

<script lang="ts">
export default { name: 'SlaConfigPage' }
</script>

<style scoped>
.sla-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.page-card {
  flex: 1;
}
.page-title {
  font-weight: 600;
  font-size: 18px;
}
</style>
