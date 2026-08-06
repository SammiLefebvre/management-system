<template>
  <div class="team-page">
    <AppCard class="page-card">
      <template #header>
        <div class="page-header">
          <span>班组排班查看</span>
          <el-button type="primary" round @click="showDialog = true">新建班组</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" @row-click="handleDetail" style="cursor:pointer">
        <el-table-column prop="teamName" label="班组名称" width="150" />
        <el-table-column prop="projectGroup" label="项目组" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" round @click.stop="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" round @click.stop="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>

    <!-- 新建/编辑班组 -->
    <el-dialog v-model="showDialog" :title="editingId ? '编辑班组' : '新建班组'" width="650px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="班组名称">
          <el-input v-model="form.teamName" placeholder="可选择或输入" />
        </el-form-item>
        <el-form-item label="班组成员">
          <el-select v-model="form.memberIds" multiple placeholder="选择外场人员" style="width:100%" filterable>
            <el-option v-for="p in externalPersonnel" :key="p.id" :label="`${p.name} (${p.phone})`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="指定司机">
          <el-select v-model="form.driverId" placeholder="从班组成员中选择" style="width:100%">
            <el-option v-for="p in externalPersonnel" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="车辆">
          <el-select v-model="form.vehicles" multiple placeholder="可选择或输入" style="width:100%" filterable allow-create />
        </el-form-item>
        <el-form-item label="排班日期">
          <el-date-picker v-model="form.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="showDialog = false">取消</el-button>
        <el-button type="primary" round @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 班组详情 -->
    <el-dialog v-model="showDetailDialog" title="班组详情" width="600px">
      <el-descriptions :column="2" border v-if="detailTeam">
        <el-descriptions-item label="班组名称">{{ detailTeam.teamName }}</el-descriptions-item>
        <el-descriptions-item label="项目组">{{ detailTeam.projectGroup }}</el-descriptions-item>
      </el-descriptions>
      <h4>成员</h4>
      <el-table :data="detailMembers" size="small">
        <el-table-column prop="personnelId" label="人员 ID" />
        <el-table-column label="是否司机">
          <template #default="{ row }">
            <StatusTag v-if="row.isDriver === 1" type="warning" label="司机" />
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <h4>车辆</h4>
      <el-tag v-for="v in detailVehicles" :key="v.id" style="margin:4px">{{ v.vehicleName }}</el-tag>
      <p v-if="detailVehicles.length === 0" style="color:#999">暂无车辆</p>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getTeamList, getTeamDetail, getTeamMembers, getTeamVehicles, createTeam, updateTeam, deleteTeam } from '@/api/team'
import { getExternalPersonnel } from '@/api/personnel'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppCard from '@/components/AppCard.vue'
import StatusTag from '@/components/StatusTag.vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const externalPersonnel = ref<any[]>([])

const showDialog = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({ teamName: '', memberIds: [] as number[], driverId: null as number | null, vehicles: [] as string[], date: '' })

const showDetailDialog = ref(false)
const detailTeam = ref<any>(null)
const detailMembers = ref<any[]>([])
const detailVehicles = ref<any[]>([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getTeamList()
    tableData.value = res.data || []
  } finally { loading.value = false }
}

async function handleDetail(row: any) {
  const [detail, members, vehicles] = await Promise.all([
    getTeamDetail(row.id), getTeamMembers(row.id), getTeamVehicles(row.id)
  ])
  detailTeam.value = detail.data
  detailMembers.value = members.data || []
  detailVehicles.value = vehicles.data || []
  showDetailDialog.value = true
}

function handleEdit(row: any) {
  editingId.value = row.id
  form.teamName = row.teamName
  showDialog.value = true
}

async function handleSave() {
  const data = { ...form, date: form.date || undefined }
  if (editingId.value) {
    await updateTeam({ ...data, id: editingId.value })
    ElMessage.success('修改成功')
  } else {
    await createTeam(data)
    ElMessage.success('创建成功')
  }
  showDialog.value = false
  editingId.value = null
  fetchData()
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    await deleteTeam(id)
    ElMessage.success('已删除')
    fetchData()
  } catch { /* 取消 */ }
}

onMounted(async () => {
  fetchData()
  const res = await getExternalPersonnel()
  externalPersonnel.value = res.data || []
})
</script>

<script lang="ts">
export default { name: 'TeamPage' }
</script>

<style scoped>
.team-page {
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
</style>
