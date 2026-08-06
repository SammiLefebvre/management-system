<template>
  <view class="container">
    <view v-if="detail" class="card">
      <view class="section-title">工单信息</view>
      <view class="info-row">
        <text class="label">工单编号:</text>
        <text>{{ detail.workOrderCode || '-' }}</text>
      </view>
      <view class="info-row">
        <text class="label">状态:</text>
        <text class="status">{{ statusLabel(detail.status) }}</text>
      </view>
      <view class="info-row">
        <text class="label">设备:</text>
        <text>{{ detail.deviceName || '-' }}</text>
      </view>
      <view class="info-row">
        <text class="label">紧急程度:</text>
        <text>{{ detail.emergencyLevel }}</text>
      </view>
      <view class="info-row">
        <text class="label">故障类型:</text>
        <text>{{ detail.faultType || '-' }}</text>
      </view>
      <view class="info-row">
        <text class="label">发布时间:</text>
        <text>{{ formatDateTime(detail.publishTime) }}</text>
      </view>

      <!-- 操作按钮 -->
      <view class="actions">
        <button v-if="detail.status === 'published'" type="primary" @click="claim">认领</button>
        <button v-if="detail.status === 'claimed'" type="primary" @click="doCheckin">开始作业（签到）</button>
        <button v-if="detail.status === 'in_progress'" type="primary" @click="doProcess">提交排查</button>
        <button v-if="detail.status === 'completing'" type="primary" @click="doComplete">提交完工</button>
        <button v-if="['published','claimed','in_progress','completing','pending_confirm'].includes(detail.status)" type="default" @click="toggleTop">{{ detail.isPriority ? '取消置顶' : '置顶' }}</button>
      </view>
    </view>

    <!-- 时间线 -->
    <view v-if="logs.length" class="card">
      <view class="section-title">操作记录</view>
      <view v-for="(log, idx) in logs" :key="idx" class="log-item">
        <view class="log-time">{{ formatDateTime(log.actionTime) }}</view>
        <view class="log-action">{{ log.remark }}</view>
        <view class="log-user">{{ log.operatorName || '系统' }}</view>
      </view>
    </view>

    <!-- 签到弹窗 -->
    <Popup v-model="showCheckin" title="签到">
      <view class="tip">请拍摄现场签到照片</view>
      <button type="primary" size="mini" @click="takeCheckinPhoto">拍照</button>
      <view v-if="checkinPhotos.length" class="photo-list">
        <image v-for="(url, idx) in checkinPhotos" :key="idx" :src="formatUrl(url)" mode="aspectFill" />
      </view>
      <button type="primary" :disabled="!checkinPhotos.length" @click="submitCheckin">提交签到</button>
    </Popup>

    <!-- 排查弹窗 -->
    <Popup v-model="showProcess" title="提交排查">
      <textarea v-model="processForm.processDesc" placeholder="填写排查过程" class="textarea" />
      <button type="primary" size="mini" @click="takeProcessPhoto">拍摄排查照片</button>
      <view v-if="processForm.processPhotos.length" class="photo-list">
        <image v-for="(url, idx) in processForm.processPhotos" :key="idx" :src="formatUrl(url)" mode="aspectFill" />
      </view>
      <button type="primary" :disabled="!processForm.processPhotos.length" @click="submitProcessData">提交排查</button>
    </Popup>

    <!-- 完工弹窗 -->
    <Popup v-model="showComplete" title="提交完工">
      <view class="form-item">
        <text class="label">维修结果:</text>
        <radio-group @change="(e) => completeForm.repairResult = e.detail.value">
          <label><radio value="fixed" :checked="completeForm.repairResult === 'fixed'" /> 已修复</label>
          <label><radio value="not_fixed" :checked="completeForm.repairResult === 'not_fixed'" /> 未修复</label>
        </radio-group>
      </view>
      <view class="form-item">
        <text class="label">故障描述:</text>
        <textarea v-model="completeForm.faultDescription" placeholder="故障现象描述" class="textarea" />
      </view>
      <view class="form-item">
        <text class="label">更换部件:</text>
        <input v-model="completeForm.replacedParts" placeholder="选填" class="input" />
      </view>
      <button type="primary" size="mini" @click="takeEndPhoto">拍摄结束照片</button>
      <view v-if="completeForm.endPhotos.length" class="photo-list">
        <image v-for="(url, idx) in completeForm.endPhotos" :key="idx" :src="formatUrl(url)" mode="aspectFill" />
      </view>
      <button type="primary" :disabled="!completeForm.endPhotos.length || !completeForm.repairResult" @click="submitCompleteData">提交完工</button>
    </Popup>
  </view>
</template>

<script setup>
import { ref, reactive, onLoad } from '@dcloudio/uni-app'
import Popup from '@/components/popup.vue'
import {
  getWorkOrderDetail,
  getWorkOrderLogs,
  claimWorkOrder,
  checkinWorkOrder,
  submitProcess,
  submitComplete,
  togglePriority
} from '@/api/workorder.js'
import { getCurrentLocation, chooseImage, formatDateTime, statusLabel } from '@/utils/common.js'
import http from '@/api/request.js'

const detail = ref(null)
const logs = ref([])
const id = ref(null)

const showCheckin = ref(false)
const checkinPhotos = ref([])
const checkinLocation = reactive({ lat: null, lng: null })

const showProcess = ref(false)
const processForm = reactive({ processDesc: '', processPhotos: [] })

const showComplete = ref(false)
const completeForm = reactive({
  repairResult: '',
  faultDescription: '',
  replacedParts: '',
  endPhotos: [],
  specialRequirements: '',
  repairerInfo: ''
})

onLoad((options) => {
  id.value = options.id
  fetchDetail()
})

async function fetchDetail() {
  const [detailRes, logRes] = await Promise.all([
    getWorkOrderDetail(id.value),
    getWorkOrderLogs(id.value)
  ])
  detail.value = detailRes.data
  logs.value = logRes.data || []
}

async function claim() {
  await claimWorkOrder(id.value)
  uni.showToast({ title: '认领成功', icon: 'success' })
  fetchDetail()
}

function formatUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:9090${url}`
}

async function doCheckin() {
  checkinPhotos.value = []
  const loc = await getCurrentLocation()
  checkinLocation.lat = loc.latitude
  checkinLocation.lng = loc.longitude
  showCheckin.value = true
}

async function takeCheckinPhoto() {
  const res = await chooseImage(1)
  const tempFilePath = res.tempFilePaths[0]
  const url = await http.upload(tempFilePath)
  checkinPhotos.value.push(url)
}

async function submitCheckin() {
  await checkinWorkOrder(id.value, {
    checkinLat: String(checkinLocation.lat),
    checkinLng: String(checkinLocation.lng),
    checkinPhotos: checkinPhotos.value
  })
  uni.showToast({ title: '签到成功', icon: 'success' })
  showCheckin.value = false
  fetchDetail()
}

function doProcess() {
  processForm.processDesc = ''
  processForm.processPhotos = []
  showProcess.value = true
}

async function takeProcessPhoto() {
  const res = await chooseImage(1)
  const url = await http.upload(res.tempFilePaths[0])
  processForm.processPhotos.push(url)
}

async function submitProcessData() {
  await submitProcess(id.value, {
    processDesc: processForm.processDesc,
    processPhotos: processForm.processPhotos
  })
  uni.showToast({ title: '提交成功', icon: 'success' })
  showProcess.value = false
  fetchDetail()
}

function doComplete() {
  Object.assign(completeForm, {
    repairResult: '',
    faultDescription: '',
    replacedParts: '',
    endPhotos: [],
    specialRequirements: '',
    repairerInfo: ''
  })
  showComplete.value = true
}

async function takeEndPhoto() {
  const res = await chooseImage(1)
  const url = await http.upload(res.tempFilePaths[0])
  completeForm.endPhotos.push(url)
}

async function submitCompleteData() {
  const userInfo = uni.getStorageSync('userInfo') || {}
  completeForm.repairerInfo = JSON.stringify({
    account: userInfo.account,
    role: userInfo.role
  })
  await submitComplete(id.value, completeForm)
  uni.showToast({ title: '提交成功', icon: 'success' })
  showComplete.value = false
  fetchDetail()
}

async function toggleTop() {
  await togglePriority(id.value)
  uni.showToast({ title: detail.value.isPriority ? '已取消置顶' : '已置顶', icon: 'none' })
  fetchDetail()
}
</script>

<style scoped>
.container { padding: 20rpx; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 20rpx; }
.section-title { font-weight: bold; font-size: 32rpx; margin-bottom: 20rpx; border-left: 8rpx solid #007aff; padding-left: 16rpx; }
.info-row { display: flex; padding: 12rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f5f5f5; }
.info-row:last-child { border-bottom: none; }
.label { color: #999; width: 160rpx; flex-shrink: 0; }
.status { color: #007aff; font-weight: bold; }
.actions { margin-top: 24rpx; display: flex; flex-direction: column; gap: 16rpx; }
.actions button { margin: 0; }
.log-item { padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.log-time { color: #999; font-size: 24rpx; }
.log-action { color: #333; font-size: 28rpx; margin-top: 6rpx; }
.log-user { color: #666; font-size: 24rpx; margin-top: 6rpx; }
.popup-content { background: #fff; padding: 40rpx; border-radius: 16rpx; width: 600rpx; }
.popup-title { font-size: 36rpx; font-weight: bold; text-align: center; margin-bottom: 20rpx; }
.tip { color: #999; margin-bottom: 20rpx; }
.photo-list { display: flex; flex-wrap: wrap; margin: 20rpx 0; }
.photo-list image { width: 160rpx; height: 160rpx; margin-right: 16rpx; margin-bottom: 16rpx; border-radius: 8rpx; }
.textarea, .input { width: 100%; border: 1rpx solid #ddd; border-radius: 8rpx; padding: 16rpx; margin: 16rpx 0; box-sizing: border-box; }
.textarea { height: 160rpx; }
.form-item { margin-bottom: 20rpx; }
.form-item label { margin-right: 20rpx; }
</style>
