<template>
  <view class="container">
    <view class="card">
      <view class="section-title">手动建单</view>

      <view class="form-item">
        <text class="label">工单类型:</text>
        <picker mode="selector" :range="workOrderTypeLabels" :value="typeIndex" @change="onTypeChange">
          <view class="picker">{{ form.workOrderType || '请选择工单类型' }}</view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label">故障点位:</text>
        <picker mode="selector" :range="deviceLabels" :value="deviceIndex" @change="onDeviceChange">
          <view class="picker">{{ selectedDevice ? selectedDevice.deviceName : '请选择故障点位' }}</view>
        </picker>
      </view>

      <view v-if="selectedDevice" class="device-info">
        <view>经度: {{ selectedDevice.longitude }} | 纬度: {{ selectedDevice.latitude }}</view>
        <view>IP: {{ selectedDevice.ip || '-' }}</view>
      </view>

      <view class="form-item">
        <text class="label">故障类型:</text>
        <input v-model="form.faultType" placeholder="可输入新类型" class="input" />
      </view>

      <view class="form-item">
        <text class="label">紧急程度:</text>
        <radio-group @change="(e) => form.emergencyLevel = e.detail.value">
          <label><radio value="一级" checked /> 一级</label>
          <label><radio value="二级" /> 二级</label>
          <label><radio value="三级" /> 三级</label>
        </radio-group>
      </view>

      <view class="form-item">
        <text class="label">参照物照片:</text>
        <button type="primary" size="mini" @click="takePhoto">拍照</button>
        <view v-if="form.referencePhoto" class="photo-list">
          <image :src="formatUrl(form.referencePhoto)" mode="aspectFill" />
        </view>
      </view>

      <view class="form-item">
        <text class="label">故障描述:</text>
        <textarea v-model="form.faultDescription" placeholder="选填" class="textarea" />
      </view>

      <view class="actions">
        <button type="default" @click="save(false)">保存草稿</button>
        <button type="primary" @click="save(true)">立即发布</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getDeviceListByType, getCodeTable } from '@/api/device.js'
import { createWorkOrder } from '@/api/workorder.js'
import { chooseImage, formatDateTime } from '@/utils/common.js'
import http from '@/api/request.js'

const workOrderTypes = ref([])
const devices = ref([])

const form = reactive({
  workOrderType: '',
  deviceId: null,
  faultType: '',
  emergencyLevel: '一级',
  referencePhoto: '',
  faultDescription: '',
  publishNow: false
})

const workOrderTypeLabels = computed(() => workOrderTypes.value.map(i => i.codeLabel))
const typeIndex = computed(() => workOrderTypes.value.findIndex(i => i.codeValue === form.workOrderType))

const deviceLabels = computed(() => devices.value.map(i => i.deviceName))
const deviceIndex = computed(() => devices.value.findIndex(i => i.id === form.deviceId))
const selectedDevice = computed(() => devices.value.find(i => i.id === form.deviceId))

onMounted(async () => {
  const res = await getCodeTable('work_order_type')
  workOrderTypes.value = res.data || []
})

async function onTypeChange(e) {
  const idx = e.detail.value
  form.workOrderType = workOrderTypes.value[idx].codeValue
  form.deviceId = null
  const res = await getDeviceListByType(form.workOrderType)
  devices.value = res.data || []
}

function onDeviceChange(e) {
  const idx = e.detail.value
  form.deviceId = devices.value[idx].id
}

async function takePhoto() {
  const res = await chooseImage(1)
  form.referencePhoto = await http.upload(res.tempFilePaths[0])
}

function formatUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:9090${url}`
}

async function save(publishNow) {
  if (!form.workOrderType || !form.deviceId) {
    return uni.showToast({ title: '请填写工单类型和故障点位', icon: 'none' })
  }
  form.publishNow = publishNow
  await createWorkOrder({ ...form })
  uni.showToast({ title: publishNow ? '发布成功' : '草稿已保存', icon: 'success' })
  setTimeout(() => uni.navigateBack(), 800)
}
</script>

<style scoped>
.container { padding: 20rpx; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; }
.section-title { font-weight: bold; font-size: 32rpx; margin-bottom: 20rpx; border-left: 8rpx solid #007aff; padding-left: 16rpx; }
.form-item { margin-bottom: 24rpx; }
.label { display: block; color: #666; margin-bottom: 12rpx; font-size: 28rpx; }
.picker, .input { border: 1rpx solid #ddd; border-radius: 8rpx; padding: 16rpx; font-size: 28rpx; }
.device-info { background: #f8f8f8; padding: 16rpx; border-radius: 8rpx; margin-bottom: 24rpx; font-size: 26rpx; color: #666; }
.textarea { width: 100%; border: 1rpx solid #ddd; border-radius: 8rpx; padding: 16rpx; height: 160rpx; box-sizing: border-box; }
.photo-list { margin-top: 16rpx; }
.photo-list image { width: 160rpx; height: 160rpx; border-radius: 8rpx; }
.actions { display: flex; gap: 20rpx; margin-top: 40rpx; }
.actions button { flex: 1; }
</style>
