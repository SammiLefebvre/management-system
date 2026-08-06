<template>
  <div class="workorder-detail">
    <AppCard class="detail-card">
      <template #header>
        <div class="page-header">
          <span>工单详情 - {{ detail?.workOrderCode || '-' }}</span>
          <el-button round @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="3" border>
        <el-descriptions-item label="工单编号">{{ detail?.workOrderCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :type="statusTagType(detail?.status)" :label="statusLabel(detail?.status)" />
        </el-descriptions-item>
        <el-descriptions-item label="紧急程度">
          <StatusTag :type="emergencyColorTag(detail?.emergencyLevel)" :label="getEmergencyLabel(detail?.emergencyLevel)" />
        </el-descriptions-item>
        <el-descriptions-item label="工单类型">{{ getCodeLabel('work_order_type', detail?.workOrderType) }}</el-descriptions-item>
        <el-descriptions-item label="故障类型">{{ getCodeLabel('fault_type', detail?.faultType) }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ detail?.publisherId }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ detail?.publishTime }}</el-descriptions-item>
        <el-descriptions-item label="认领人">{{ detail?.claimerId }}</el-descriptions-item>
        <el-descriptions-item label="认领时间">{{ detail?.claimTime }}</el-descriptions-item>
        <el-descriptions-item label="响应时长" v-if="detail?.responseDuration !== null">
          {{ detail?.responseDuration }} 分钟
        </el-descriptions-item>
        <el-descriptions-item label="修复时长" v-if="detail?.repairDuration !== null">
          {{ detail?.repairDuration }} 分钟
        </el-descriptions-item>
      </el-descriptions>

      <div class="detail-actions">
        <el-button v-if="canDispatch" type="primary" round :loading="aiLoading" @click="handleAiDispatch">AI 派单建议</el-button>
        <el-button v-if="detail?.status==='published'" type="primary" round @click="handleClaim">认领</el-button>
        <el-button v-if="detail?.status==='claimed' && detail?.claimerId===userStore.userInfo.userId" type="warning" round @click="handleCancelClaim">取消认领</el-button>
        <el-button v-if="detail?.status==='claimed'" type="primary" round @click="showCheckinDialog=true">签到</el-button>
        <el-button v-if="detail?.status==='in_progress'" type="primary" round @click="showProcessDialog=true">提交排查</el-button>
        <el-button v-if="detail?.status==='completing'" type="success" round @click="showCompleteDialog=true">提交完工</el-button>
        <el-button v-if="detail?.status==='pending_confirm'" type="primary" round @click="handleConfirm">确认完成</el-button>
        <el-button v-if="detail?.status==='pending_force_close'" type="danger" round @click="handleConfirmForceClose">确认强制关闭</el-button>
        <el-button v-if="canForceClose" type="danger" round @click="handleForceClose">发起强制关闭</el-button>
      </div>
    </AppCard>

    <AppCard class="logs-card">
      <template #header>
        <span class="page-title">操作日志</span>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="log in logs"
          :key="log.id"
          :timestamp="log.actionTime"
          placement="top"
        >
          <strong>{{ log.operatorName }}</strong> - {{ log.action }} - {{ log.remark }}
        </el-timeline-item>
        <el-timeline-item v-if="logs.length === 0" placement="top">
          暂无操作记录
        </el-timeline-item>
      </el-timeline>
    </AppCard>

    <el-dialog v-model="showCheckinDialog" title="签到 - 开始作业" width="500px">
      <el-form label-width="100px">
        <el-form-item label="签到照片">
          <el-upload :action="uploadUrl" :headers="uploadHeaders" list-type="picture-card"
            :on-success="(res:any) => checkinPhotos.push(res.data)" :auto-upload="true">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="showCheckinDialog=false">取消</el-button>
        <el-button type="primary" round @click="handleCheckin">确认签到</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showProcessDialog" title="提交排查" width="500px">
      <el-form label-width="100px">
        <el-form-item label="排查描述">
          <el-input v-model="processDesc" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="排查照片">
          <el-upload :action="uploadUrl" :headers="uploadHeaders" list-type="picture-card"
            :on-success="(res:any) => processPhotos.push(res.data)" :auto-upload="true">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="showProcessDialog=false">取消</el-button>
        <el-button type="primary" round @click="handleSubmitProcess" :disabled="processPhotos.length===0">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCompleteDialog" title="提交完工" width="600px">
      <el-form label-width="100px">
        <el-form-item label="结束照片">
          <el-upload :action="uploadUrl" :headers="uploadHeaders" list-type="picture-card"
            :on-success="(res:any) => endPhotos.push(res.data)" :auto-upload="true">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="维修结果">
          <el-radio-group v-model="repairResult">
            <el-radio value="fixed">已修复</el-radio>
            <el-radio value="not_fixed">未修复</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="故障描述">
          <el-input v-model="faultDesc" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="更换部件">
          <el-input v-model="replacedParts" placeholder="如有更换请填写" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="showCompleteDialog=false">取消</el-button>
        <el-button type="primary" round @click="handleSubmitComplete" :disabled="endPhotos.length===0||!repairResult">提交完工</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="showAiDispatchDialog" title="AI 派单建议" width="520px">
      <div v-if="aiAdvice" class="ai-advice">
        <div class="ai-recommend">
          <div class="ai-recommend-title">推荐工程师</div>
          <div class="ai-recommend-name">{{ aiAdvice.name }}</div>
        </div>
        <div class="ai-reason">
          <div class="ai-reason-title">推荐理由</div>
          <p>{{ aiAdvice.reason }}</p>
        </div>
      </div>
      <template #footer>
        <el-button round @click="showAiDispatchDialog=false">关闭</el-button>
        <el-button type="primary" round :disabled="!aiAdvice" @click="handleAssign">一键指派</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCodeTable } from '@/composables/useCodeTable'
import { useUserStore } from '@/store/user'
import {
  getWorkOrderDetail, getWorkOrderLogs,
  claimWorkOrder, cancelClaimWorkOrder,
  checkinWorkOrder, submitProcessWorkOrder, submitCompleteWorkOrder,
  confirmWorkOrder, forceCloseWorkOrder, confirmForceCloseWorkOrder
} from '@/api/workorder'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dispatchAdvice, assignWorkOrder, type DispatchAdvice } from '@/api/ai'
import { ensureHuggingFaceToken } from '@/composables/useHuggingFaceToken'
import AppCard from '@/components/AppCard.vue'
import StatusTag from '@/components/StatusTag.vue'

const { load: loadCodeTable, getCodeLabel, emergencyTagType, getEmergencyLabel } = useCodeTable()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const id = Number(route.params.id)
const detail = ref<any>(null)
const logs = ref<any[]>([])
const uploadUrl = 'http://localhost:9090/api/file/upload'
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${localStorage.getItem('token')}` }))

const showCheckinDialog = ref(false)
const showProcessDialog = ref(false)
const showCompleteDialog = ref(false)
const showAiDispatchDialog = ref(false)
const aiLoading = ref(false)
const aiAdvice = ref<DispatchAdvice | null>(null)

const checkinPhotos = ref<string[]>([])
const processDesc = ref('')
const processPhotos = ref<string[]>([])
const endPhotos = ref<string[]>([])
const repairResult = ref('')
const faultDesc = ref('')
const replacedParts = ref('')

const canForceClose = computed(() => {
  if (!detail.value) return false
  const role = userStore.userInfo.role
  return (role === '公司管理' || role === '项目管理') && !['closed', 'pending_force_close'].includes(detail.value.status)
})

const canDispatch = computed(() => {
  if (!detail.value) return false
  const role = userStore.userInfo.role
  return (role === '公司管理' || role === '项目管理') && detail.value.status === 'published'
})

function statusLabel(s: string) {
  const map: Record<string,string> = { draft:'草稿', published:'待认领', claimed:'进行中-待签到',
    in_progress:'作业中', completing:'待完工', pending_confirm:'待确认',
    confirmed:'已确认', pending_force_close:'待关闭确认', closed:'已关闭' }
  return map[s] || s
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

async function fetchData() {
  const [detailRes, logsRes] = await Promise.all([getWorkOrderDetail(id), getWorkOrderLogs(id)])
  detail.value = detailRes.data
  logs.value = logsRes.data || []
}

async function handleClaim() { await claimWorkOrder(id); ElMessage.success('已认领'); fetchData() }
async function handleCancelClaim() { await cancelClaimWorkOrder(id); ElMessage.success('已取消认领'); fetchData() }

async function handleCheckin() {
  if (checkinPhotos.value.length === 0) return ElMessage.warning('请上传签到照片')
  await checkinWorkOrder(id, { checkinPhotos: checkinPhotos.value, checkinLat: '0', checkinLng: '0' })
  ElMessage.success('签到成功')
  showCheckinDialog.value = false
  fetchData()
}

async function handleSubmitProcess() {
  await submitProcessWorkOrder(id, { processDesc: processDesc.value, processPhotos: processPhotos.value })
  ElMessage.success('排查已提交')
  showProcessDialog.value = false
  fetchData()
}

async function handleSubmitComplete() {
  await submitCompleteWorkOrder(id, {
    endPhotos: endPhotos.value,
    repairResult: repairResult.value,
    faultDescription: faultDesc.value,
    replacedParts: replacedParts.value
  })
  ElMessage.success('完工已提交，等待内场确认')
  showCompleteDialog.value = false
  fetchData()
}

async function handleConfirm() { await confirmWorkOrder(id); ElMessage.success('已确认完成'); fetchData() }
async function handleConfirmForceClose() { await confirmForceCloseWorkOrder(id); ElMessage.success('已确认关闭'); fetchData() }

async function handleForceClose() {
  try {
    const { value: reason } = await ElMessageBox.prompt('强制关闭原因', '强制关闭')
    if (reason) { await forceCloseWorkOrder(id, reason); ElMessage.success('已发起强制关闭'); fetchData() }
  } catch { /* 取消 */ }
}

async function handleAiDispatch() {
  try {
    await ensureHuggingFaceToken()
  } catch {
    return
  }
  aiLoading.value = true
  try {
    const res = await dispatchAdvice(id)
    aiAdvice.value = res.data
    showAiDispatchDialog.value = true
  } finally {
    aiLoading.value = false
  }
}

async function handleAssign() {
  if (!aiAdvice.value) return
  await assignWorkOrder(id, aiAdvice.value.personnelId)
  ElMessage.success('已指派给 ' + aiAdvice.value.name)
  showAiDispatchDialog.value = false
  fetchData()
}

onMounted(async () => {
  await Promise.all([
    loadCodeTable('emergency_level'),
    loadCodeTable('work_order_type'),
    loadCodeTable('fault_type')
  ])
  fetchData()
})
</script>

<script lang="ts">
export default { name: 'WorkOrderDetail' }
</script>

<style scoped>
.workorder-detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 18px;
}
.page-title {
  font-weight: 600;
  font-size: 18px;
}
.detail-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.ai-advice {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.ai-recommend {
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(0,113,227,0.1), rgba(41,151,255,0.1));
}
.ai-recommend-title {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}
.ai-recommend-name {
  font-size: 22px;
  font-weight: 700;
  color: var(--accent);
}
.ai-reason-title {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.ai-reason p {
  line-height: 1.6;
  color: var(--text-primary);
  margin: 0;
}
</style>
