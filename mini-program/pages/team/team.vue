<template>
  <view class="container">
    <view class="card">
      <view class="section-title">我的班组</view>
      <view v-for="team in teamList" :key="team.id" class="team-card" @click="openEdit(team)">
        <view class="team-name">{{ team.teamName }}</view>
        <view class="team-info">
          <view>成员: {{ getMemberNames(team.id) }}</view>
          <view>车辆: {{ getVehicleNames(team.id) }}</view>
        </view>
        <view class="edit-hint">点击编辑</view>
      </view>
      <view v-if="teamList.length === 0" class="empty">暂无班组，点击下方新建</view>
    </view>

    <button type="primary" style="margin-top: 30rpx" @click="openCreate">新建班组</button>

    <!-- 新建/编辑班组弹窗 -->
    <Popup v-model="showForm" :title="editingId ? '编辑班组' : '新建班组'">
      <view class="form-item">
        <text class="label">班组名称:</text>
        <input v-model="form.teamName" placeholder="请输入班组名称" class="input" />
      </view>

      <view class="form-item">
        <text class="label">班组成员:</text>
        <view v-for="p in externalPersonnel" :key="p.id" class="checkbox-item">
          <checkbox :value="String(p.id)" :checked="form.memberIds.includes(p.id)" @click="toggleMember(p.id)" />
          <text>{{ p.name }}</text>
        </view>
      </view>

      <view class="form-item">
        <text class="label">司机:</text>
        <picker
          mode="selector"
          :range="driverOptions"
          :value="driverIndex"
          :disabled="form.memberIds.length === 0"
          @change="onDriverChange"
        >
          <view class="picker" :class="{ disabled: form.memberIds.length === 0 }">
            {{ form.memberIds.length === 0 ? '请先选择成员' : (selectedDriverName || '请选择司机') }}
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label">车辆:</text>
        <input v-model="vehicleInput" placeholder="可输入多个，用逗号分隔" class="input" />
      </view>

      <view class="form-item">
        <text class="label">排班日期:</text>
        <picker mode="date" :value="form.date" @change="(e) => form.date = e.detail.value">
          <view class="picker">{{ form.date || '请选择日期' }}</view>
        </picker>
      </view>

      <button type="primary" @click="submit">保存</button>
    </Popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getTeamList, createTeam, updateTeam, getTeamDetail, getExternalPersonnel } from '@/api/team.js'
import Popup from '@/components/popup.vue'

const teamList = ref([])
const teamMembers = ref({})
const teamVehicles = ref({})
const externalPersonnel = ref([])
const showForm = ref(false)
const editingId = ref(null)
const vehicleInput = ref('')

const form = reactive({
  teamName: '',
  memberIds: [],
  driverId: null,
  vehicles: [],
  date: new Date().toISOString().split('T')[0]
})

const driverOptions = computed(() => externalPersonnel.value.filter(p => form.memberIds.includes(p.id)).map(p => p.name))
const driverIds = computed(() => externalPersonnel.value.filter(p => form.memberIds.includes(p.id)).map(p => p.id))
const driverIndex = computed(() => Math.max(0, driverIds.value.indexOf(form.driverId)))
const selectedDriverName = computed(() => {
  const p = externalPersonnel.value.find(i => i.id === form.driverId)
  return p ? p.name : ''
})

onMounted(() => {
  fetchData()
})

async function fetchData() {
  const [teamRes, personRes] = await Promise.all([
    getTeamList(),
    getExternalPersonnel()
  ])
  teamList.value = teamRes.data || []
  externalPersonnel.value = personRes.data || []

  for (const team of teamList.value) {
    const detail = await getTeamDetail(team.id)
    teamMembers.value[team.id] = detail.data.members || []
    teamVehicles.value[team.id] = detail.data.vehicles || []
  }
}

function getMemberNames(teamId) {
  const members = teamMembers.value[teamId] || []
  if (members.length === 0) return '-'
  return members.map(m => m.personnelName || m.personnelId).join(', ')
}

function getVehicleNames(teamId) {
  const vehicles = teamVehicles.value[teamId] || []
  if (vehicles.length === 0) return '-'
  return vehicles.map(v => v.vehicleName).join(', ')
}

function toggleMember(id) {
  const idx = form.memberIds.indexOf(id)
  if (idx > -1) {
    form.memberIds.splice(idx, 1)
    if (form.driverId === id) form.driverId = null
  } else {
    form.memberIds.push(id)
  }
}

function onDriverChange(e) {
  form.driverId = driverIds.value[e.detail.value]
}

function openCreate() {
  resetForm()
  editingId.value = null
  showForm.value = true
}

async function openEdit(team) {
  resetForm()
  editingId.value = team.id
  form.teamName = team.teamName || ''
  const members = teamMembers.value[team.id] || []
  form.memberIds = members.map(m => m.personnelId).filter(Boolean)
  const driver = members.find(m => m.isDriver === 1 || m.isDriver === true)
  form.driverId = driver ? driver.personnelId : null
  if (members[0]?.date) {
    form.date = String(members[0].date).slice(0, 10)
  }
  const vehicles = teamVehicles.value[team.id] || []
  vehicleInput.value = vehicles.map(v => v.vehicleName).join(',')
  showForm.value = true
}

async function submit() {
  if (!form.teamName || form.memberIds.length === 0) {
    return uni.showToast({ title: '请填写班组名称和成员', icon: 'none' })
  }
  form.vehicles = vehicleInput.value.split(',').map(v => v.trim()).filter(Boolean)
  if (editingId.value) {
    await updateTeam({ id: editingId.value, ...form })
    uni.showToast({ title: '更新成功', icon: 'success' })
  } else {
    await createTeam({ ...form })
    uni.showToast({ title: '创建成功', icon: 'success' })
  }
  showForm.value = false
  resetForm()
  fetchData()
}

function resetForm() {
  form.teamName = ''
  form.memberIds = []
  form.driverId = null
  form.vehicles = []
  form.date = new Date().toISOString().split('T')[0]
  vehicleInput.value = ''
  editingId.value = null
}
</script>

<style scoped>
.container { padding: 20rpx; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; }
.section-title { font-weight: bold; font-size: 32rpx; margin-bottom: 20rpx; border-left: 8rpx solid #007aff; padding-left: 16rpx; }
.team-card { background: #f8f8f8; padding: 20rpx; border-radius: 10rpx; margin-bottom: 20rpx; }
.team-name { font-weight: bold; font-size: 30rpx; margin-bottom: 12rpx; }
.team-info { font-size: 26rpx; color: #666; line-height: 1.6; }
.edit-hint { margin-top: 8rpx; font-size: 22rpx; color: #007aff; }
.empty { text-align: center; color: #999; padding: 60rpx 0; }
.form-item { margin-bottom: 24rpx; }
.label { display: block; color: #666; margin-bottom: 12rpx; font-size: 28rpx; }
.input, .picker { border: 1rpx solid #ddd; border-radius: 8rpx; padding: 16rpx; font-size: 28rpx; }
.picker.disabled { color: #bbb; background: #f5f5f5; }
.checkbox-item { display: flex; align-items: center; margin-bottom: 12rpx; }
.checkbox-item text { margin-left: 12rpx; font-size: 28rpx; }
</style>
