<template>
  <div>
    <el-page-header content="我的预约" @back="$router.push('/')" />
    <el-table :data="appointments" style="width: 100%; margin-top: 16px" v-loading="loading">
      <el-table-column prop="hospitalName" label="医院" min-width="140" />
      <el-table-column prop="departmentName" label="科室" width="110" />
      <el-table-column prop="doctorName" label="医生" width="100" />
      <el-table-column label="就诊时间" width="170">
        <template #default="{ row }">{{ formatDate(row.workDate) }} {{ row.timeSlotName }}</template>
      </el-table-column>
      <el-table-column prop="patientName" label="就诊人" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="110">
        <template #default="{ row }">
          <el-popconfirm v-if="row.status === 1" title="确认取消该预约吗？" @confirm="doCancel(row)">
            <template #reference>
              <el-button size="small" type="danger" plain>取消预约</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && appointments.length === 0" description="暂无预约记录" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { cancelAppointment, getMyAppointments } from '../api'

const appointments = ref([])
const loading = ref(false)

function formatDate(d) {
  return d ? String(d).slice(0, 10) : ''
}

function statusName(status) {
  return ({ 1: '待就诊', 2: '已完成', 3: '已取消' })[status] || '未知'
}

function statusType(status) {
  return ({ 1: 'primary', 2: 'success', 3: 'info' })[status] || 'info'
}

async function load() {
  loading.value = true
  try {
    appointments.value = (await getMyAppointments({ current: 1, size: 50 })) || []
  } finally {
    loading.value = false
  }
}

async function doCancel(row) {
  await cancelAppointment(row.appointmentId, '个人原因取消')
  ElMessage.success('取消成功，号源已释放')
  await load()
}

onMounted(load)
</script>
