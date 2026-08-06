<template>
  <view class="container">
    <!-- 登录提示 -->
    <view v-if="!token" class="login-box">
      <view class="title">工单管理系统</view>
      <view class="tip">请使用微信一键登录</view>
      <button type="primary" @click="handleLogin">微信登录</button>
    </view>

    <view v-else>
      <!-- 顶部 Tab -->
      <view class="tabs">
        <view
          class="tab-item"
          :class="{ active: currentTab === 0 }"
          @click="currentTab = 0"
        >待发布</view>
        <view
          class="tab-item"
          :class="{ active: currentTab === 1 }"
          @click="currentTab = 1"
        >处理中</view>
        <view
          class="tab-item"
          :class="{ active: currentTab === 2 }"
          @click="currentTab = 2"
        >已确认</view>
      </view>

      <!-- 待发布 Tab -->
      <view v-if="currentTab === 0" class="tab-content">
        <button type="primary" style="margin-bottom: 16rpx" @click="goCreate">手动建单</button>
        <view v-for="item in draftList" :key="item.id" class="card" @click="goDetail(item.id)">
          <view class="card-header">
            <text class="code">{{ item.workOrderCode || '草稿' }}</text>
            <text class="tag">待发布</text>
          </view>
          <view class="card-body">
            <view>设备: {{ item.deviceName || '-' }}</view>
            <view>紧急: {{ item.emergencyLevel }}</view>
            <view>时间: {{ formatDateTime(item.createdAt) }}</view>
          </view>
        </view>
        <view v-if="draftList.length === 0" class="empty">暂无草稿</view>
      </view>

      <!-- 处理中 Tab -->
      <view v-if="currentTab === 1" class="tab-content">
        <view v-for="item in processingList" :key="item.id" class="card" @click="goDetail(item.id)">
          <view class="card-header">
            <text class="code">{{ item.workOrderCode }}</text>
            <text class="tag" :class="item.status">{{ statusLabel(item.status) }}</text>
          </view>
          <view class="card-body">
            <view>设备: {{ item.deviceName || '-' }}</view>
            <view>紧急: {{ item.emergencyLevel }}</view>
            <view v-if="item.isPriority" class="priority">置顶</view>
          </view>
        </view>
        <view v-if="processingList.length === 0" class="empty">暂无处理中工单</view>
      </view>

      <!-- 已确认 Tab -->
      <view v-if="currentTab === 2" class="tab-content">
        <view v-for="item in confirmedList" :key="item.id" class="card" @click="goDetail(item.id)">
          <view class="card-header">
            <text class="code">{{ item.workOrderCode }}</text>
            <text class="tag confirmed">已确认</text>
          </view>
          <view class="card-body">
            <view>设备: {{ item.deviceName || '-' }}</view>
            <view>确认时间: {{ formatDateTime(item.confirmTime) }}</view>
          </view>
        </view>
        <view v-if="confirmedList.length === 0" class="empty">暂无已确认工单</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { wxLogin } from '@/api/auth.js'
import { getWorkOrderList } from '@/api/workorder.js'
import { formatDateTime, statusLabel } from '@/utils/common.js'

const { token, setToken, setUserInfo } = useUserStore()
const currentTab = ref(1)
const list = ref([])

const draftList = computed(() => list.value.filter(i => i.status === 'draft'))
const processingList = computed(() => list.value.filter(i => ['published','claimed','in_progress','completing','pending_confirm'].includes(i.status)))
const confirmedList = computed(() => list.value.filter(i => i.status === 'confirmed' || i.status === 'closed'))

onShow(() => {
  if (token.value) fetchList()
})

onPullDownRefresh(() => {
  fetchList().finally(() => uni.stopPullDownRefresh())
})

async function handleLogin() {
  try {
    const loginRes = await uni.login({ provider: 'weixin' })
    const res = await wxLogin(loginRes.code)
    setToken(res.data.token)
    setUserInfo({
      userId: res.data.userId,
      account: res.data.account,
      role: res.data.role,
      projectGroup: res.data.projectGroup
    })
    uni.showToast({ title: '登录成功', icon: 'success' })
    fetchList()
  } catch (e) {
    console.error(e)
  }
}

async function fetchList() {
  const res = await getWorkOrderList({ page: 1, size: 100 })
  // 排序：置顶 → 一级 → 二级 → 三级 → 发布时间倒序
  const levelOrder = { '一级': 1, '二级': 2, '三级': 3 }
  list.value = (res.data.records || []).sort((a, b) => {
    if (b.isPriority !== a.isPriority) return b.isPriority - a.isPriority
    const la = levelOrder[a.emergencyLevel] || 9
    const lb = levelOrder[b.emergencyLevel] || 9
    if (la !== lb) return la - lb
    return new Date(b.publishTime || b.createdAt) - new Date(a.publishTime || a.createdAt)
  })
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/workorder/detail?id=${id}` })
}

function goCreate() {
  uni.navigateTo({ url: '/pages/create/create' })
}
</script>

<style scoped>
.container { padding: 20rpx; }
.login-box { text-align: center; margin-top: 200rpx; }
.title { font-size: 48rpx; font-weight: bold; margin-bottom: 20rpx; }
.tip { color: #999; margin-bottom: 40rpx; }
.tabs { display: flex; border-bottom: 1rpx solid #eee; margin-bottom: 20rpx; }
.tab-item { flex: 1; text-align: center; padding: 24rpx 0; color: #666; }
.tab-item.active { color: #007aff; border-bottom: 4rpx solid #007aff; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 20rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.code { font-weight: bold; font-size: 30rpx; }
.tag { font-size: 24rpx; padding: 6rpx 12rpx; border-radius: 8rpx; background: #e6f2ff; color: #007aff; }
.tag.published { background: #fff0e6; color: #ff9500; }
.tag.claimed, .tag.in_progress, .tag.completing { background: #e6f7ff; color: #1890ff; }
.tag.pending_confirm { background: #fff7e6; color: #fa8c16; }
.tag.confirmed, .tag.closed { background: #f6ffed; color: #52c41a; }
.card-body { font-size: 28rpx; color: #666; line-height: 1.8; }
.priority { color: #ff4d4f; font-weight: bold; margin-top: 8rpx; }
.empty { text-align: center; color: #999; padding: 100rpx 0; }
</style>
