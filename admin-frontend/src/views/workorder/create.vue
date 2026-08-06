<template>
  <div class="workorder-create">
    <AppCard class="form-card">
      <template #header>
        <div class="page-header">
          <span>新建工单</span>
          <el-button round @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-form :model="form" label-width="120px" style="max-width:700px">
        <el-form-item label="工单类型" required>
          <el-select v-model="form.workOrderType" placeholder="请选择" style="width:100%" @change="handleTypeChange">
            <el-option v-for="item in workOrderTypes" :key="item.codeValue" :label="item.codeLabel" :value="item.codeValue" />
          </el-select>
        </el-form-item>

        <el-form-item label="故障点位" required>
          <el-select v-model="form.deviceId" placeholder="请先选择工单类型" style="width:100%" filterable>
            <el-option v-for="d in devices" :key="d.id" :label="`${d.deviceName} (${d.ip})`" :value="d.id" />
          </el-select>
          <div v-if="selectedDevice" class="device-hint">
            经度: {{ selectedDevice.longitude }} | 纬度: {{ selectedDevice.latitude }} | IP: {{ selectedDevice.ip }}
          </div>
        </el-form-item>

        <el-form-item label="故障类型">
          <el-select v-model="form.faultType" placeholder="可选择或输入" style="width:100%" filterable allow-create>
            <el-option v-for="item in faultTypes" :key="item.codeValue" :label="item.codeLabel" :value="item.codeValue" />
          </el-select>
        </el-form-item>

        <el-form-item label="紧急程度" required>
          <el-radio-group v-model="form.emergencyLevel">
            <el-radio-button
              v-for="item in emergencyLevels"
              :key="item.codeValue"
              :value="item.codeValue"
            >
              {{ item.codeLabel }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="参照物照片">
          <el-upload :action="uploadUrl" :headers="uploadHeaders" :on-success="handleUploadSuccess"
            list-type="picture-card" :limit="1" :auto-upload="true">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="故障描述">
          <el-input v-model="form.faultDescription" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" round @click="handleSave(false)">保存草稿</el-button>
          <el-button type="success" size="large" round @click="handleSave(true)">立即发布</el-button>
        </el-form-item>
      </el-form>
    </AppCard>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useCodeTable, type CodeItem } from '@/composables/useCodeTable'
import { getDeviceListByType } from '@/api/device'
import { createWorkOrder } from '@/api/workorder'
import { ElMessage } from 'element-plus'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const { load: loadCodeTable, getCached } = useCodeTable()
const devices = ref<any[]>([])
const uploadUrl = 'http://localhost:9090/api/file/upload'
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${localStorage.getItem('token')}` }))

const workOrderTypes = computed<CodeItem[]>(() => getCached('work_order_type'))
const faultTypes = computed<CodeItem[]>(() => getCached('fault_type'))
const emergencyLevels = computed<CodeItem[]>(() => getCached('emergency_level'))

const form = reactive({
  workOrderType: '',
  deviceId: null as number | null,
  faultType: '',
  faultDescription: '',
  emergencyLevel: '',
  referencePhoto: ''
})

const selectedDevice = computed(() => devices.value.find((d: any) => d.id === form.deviceId))

async function handleTypeChange() {
  form.deviceId = null
  if (form.workOrderType) {
    const res = await getDeviceListByType(form.workOrderType)
    devices.value = res.data || []
  }
}

function handleUploadSuccess(res: any) {
  form.referencePhoto = res.data
}

async function handleSave(publishNow: boolean) {
  if (!form.workOrderType || !form.deviceId) {
    return ElMessage.warning('请填写工单类型和故障点位')
  }
  try {
    await createWorkOrder({ ...form, publishNow })
    ElMessage.success(publishNow ? '工单已发布' : '草稿已保存')
    router.push('/work-order')
  } catch { /* handled */ }
}

onMounted(async () => {
  await Promise.all([
    loadCodeTable('work_order_type'),
    loadCodeTable('fault_type'),
    loadCodeTable('emergency_level')
  ])
})

watch(emergencyLevels, (list) => {
  if (list.length && !form.emergencyLevel) {
    const defaultIndex = Math.min(1, list.length - 1)
    form.emergencyLevel = list[defaultIndex]?.codeValue ?? ''
  }
}, { immediate: true })
</script>

<script lang="ts">
export default { name: 'WorkOrderCreate' }
</script>

<style scoped>
.workorder-create {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.form-card {
  flex: 1;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 18px;
}
.device-hint {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
}
</style>
