<template>
  <div class="workorder-list">
    <AppCard class="filter-card">
      <el-form :inline="true" :model="query" size="default">
        <el-form-item label="故障类型">
          <el-input v-model="query.faultType" placeholder="模糊搜索" clearable style="width:140px" />
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-select v-model="query.emergencyLevel" placeholder="全部" clearable style="width:120px">
            <el-option
              v-for="item in emergencyLevels"
              :key="item.codeValue"
              :label="item.codeLabel"
              :value="item.codeValue"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:200px">
            <el-option label="待认领" value="published" />
            <el-option label="进行中" value="claimed,in_progress,completing" />
            <el-option label="待确认" value="pending_confirm" />
            <el-option label="已确认" value="confirmed" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width:260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" round @click="handleQuery">查询</el-button>
          <el-button round @click="handleReset">重置</el-button>
          <el-button type="success" round @click="$router.push('/work-order/create')">新建工单</el-button>
        </el-form-item>
      </el-form>
    </AppCard>

    <AppCard class="table-card">
      <el-table :data="tableData" v-loading="loading" @row-click="(row: any) => $router.push(`/work-order/${row.id}`)" style="cursor:pointer">
        <el-table-column prop="workOrderCode" label="工单编号" width="200" />
        <el-table-column prop="faultDescription" label="故障点位" show-overflow-tooltip />
        <el-table-column prop="emergencyLevel" label="紧急程度" width="100">
          <template #default="{ row }">
            <StatusTag :type="emergencyColorTag(row.emergencyLevel)" :label="getEmergencyLabel(row.emergencyLevel)" />
          </template>
        </el-table-column>
        <el-table-column prop="workOrderType" label="工单类型" width="120">
          <template #default="{ row }">
            {{ getCodeLabel('work_order_type', row.workOrderType) }}
          </template>
        </el-table-column>
        <el-table-column prop="faultType" label="故障类型" width="120">
          <template #default="{ row }">
            {{ getCodeLabel('fault_type', row.faultType) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <StatusTag :type="statusTagType(row.status)" :label="statusLabel(row.status)" />
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170" />
        <el-table-column label="SLA" width="100">
          <template #default="{ row }">
            <StatusTag :type="isSlaOverdue(row) ? 'danger' : 'success'" :label="isSlaOverdue(row) ? '超时' : '正常'" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" round @click.stop="$router.push(`/work-order/${row.id}`)">详情</el-button>
            <el-button
              v-if="row.status === 'published'"
              size="small"
              type="warning"
              round
              @click.stop="handleForceClose(row)"
            >强制关闭</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10,20,50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </AppCard>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useCodeTable, type CodeItem } from '@/composables/useCodeTable'
import { getWorkOrderPage, forceCloseWorkOrder } from '@/api/workorder'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppCard from '@/components/AppCard.vue'
import StatusTag from '@/components/StatusTag.vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dateRange = ref([])
const query = reactive({ faultType: '', emergencyLevel: '', status: '', page: 1, size: 10 })
const { load: loadCodeTable, getCached, getCodeLabel, emergencyTagType, getEmergencySeverity, getEmergencyLabel } = useCodeTable()
const emergencyLevels = computed<CodeItem[]>(() => getCached('emergency_level'))

function statusLabel(status: string) {
  const map: Record<string, string> = {
    draft: '草稿', published: '待认领', claimed: '进行中',
    in_progress: '作业中', completing: '待完工', pending_confirm: '待确认',
    confirmed: '已确认', pending_force_close: '待关闭确认', closed: '已关闭'
  }
  return map[status] || status
}

function statusTagType(status: string): 'success' | 'warning' | 'danger' | 'info' | 'purple' {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info' | 'purple'> = {
    published: 'info',
    claimed: 'warning',
    in_progress: 'warning',
    completing: 'warning',
    confirmed: 'success',
    pending_confirm: 'purple',
    closed: 'info',
    pending_force_close: 'danger',
    draft: 'info'
  }
  return map[status] || 'info'
}

function emergencyColorTag(level: string): 'success' | 'warning' | 'danger' | 'info' | 'purple' {
  const tag = emergencyTagType(level)
  if (tag === 'danger') return 'danger'
  if (tag === 'warning') return 'warning'
  return 'info'
}

function isSlaOverdue(row: any) {
  if (!row.publishTime) return false
  const now = Date.now()
  const pubTime = new Date(row.publishTime).getTime()
  const diffMin = Math.floor((now - pubTime) / 60000)
  if (row.status === 'published' && diffMin > 60) return true
  const thresholds = [240, 120, 60]
  const severity = getEmergencySeverity(row.emergencyLevel)
  const threshold = thresholds[severity] ?? 60
  if (!['confirmed', 'closed'].includes(row.status) && diffMin > threshold) return true
  return false
}

async function handleQuery() {
  loading.value = true
  try {
    const params: any = { ...query }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    } else {
      params.startDate = null
      params.endDate = null
    }
    const res = await getWorkOrderPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleReset() {
  query.faultType = ''
  query.emergencyLevel = ''
  query.status = ''
  dateRange.value = []
  query.page = 1
  handleQuery()
}

async function handleForceClose(row: any) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入强制关闭原因', '强制关闭', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    if (reason) {
      await forceCloseWorkOrder(row.id, reason)
      ElMessage.success('已发起强制关闭')
      handleQuery()
    }
  } catch { /* 用户取消 */ }
}

onMounted(async () => {
  await Promise.all([
    loadCodeTable('emergency_level'),
    loadCodeTable('work_order_type'),
    loadCodeTable('fault_type')
  ])
  handleQuery()
})
</script>

<script lang="ts">
export default { name: 'WorkOrderList' }
</script>

<style scoped>
.workorder-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.filter-card :deep(.app-card-body) {
  padding-bottom: 12px;
}
.table-card {
  flex: 1;
}
.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>
