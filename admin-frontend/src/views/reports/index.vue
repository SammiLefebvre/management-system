<template>
  <div class="reports-page">
    <AppCard class="report-card">
      <template #header><span>报表导出</span></template>
      <el-form :model="form" label-width="100px" style="max-width:600px">
        <el-form-item label="数据类型">
          <el-radio-group v-model="form.dataType">
            <el-radio-button value="work_order">工单</el-radio-button>
            <el-radio-button value="device">设备</el-radio-button>
            <el-radio-button value="personnel">人员</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="格式">
          <el-radio-group v-model="format">
            <el-radio-button value="excel">Excel</el-radio-button>
            <el-radio-button value="pdf">PDF</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" round :loading="loading" @click="handleExport">导出报表</el-button>
        </el-form-item>
      </el-form>
    </AppCard>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import AppCard from '@/components/AppCard.vue'
import { exportExcel, exportPdf, type ReportQuery } from '@/api/reports'
import { ElMessage } from 'element-plus'

const form = reactive<ReportQuery>({
  dataType: 'work_order',
  startDate: '',
  endDate: '',
})
const format = ref<'excel' | 'pdf'>('excel')
const loading = ref(false)
const dateRange = ref<string[]>([])

function download(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

async function handleExport() {
  if (dateRange.value?.length === 2) {
    form.startDate = dateRange.value[0] || ''
    form.endDate = dateRange.value[1] || ''
  }
  loading.value = true
  try {
    const blob = format.value === 'excel'
      ? await exportExcel(form)
      : await exportPdf(form)
    const ext = format.value === 'excel' ? 'xlsx' : 'pdf'
    download(blob, `${form.dataType}_report_${Date.now()}.${ext}`)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}
</script>

<script lang="ts">
export default { name: 'ReportsPage' }
</script>

<style scoped>
.reports-page { display: flex; justify-content: center; padding-top: 40px; }
.report-card { width: 100%; max-width: 720px; }
</style>
