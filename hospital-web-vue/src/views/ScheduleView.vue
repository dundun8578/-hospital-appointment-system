<template>
  <div>
    <el-page-header :content="`${doctorName} 未来7天排班`" @back="$router.back()" />
    <el-row :gutter="12" style="margin-top: 16px">
      <el-col v-for="schedule in schedules" :key="schedule.scheduleId" :xs="12" :sm="8" :md="6" class="card-col">
        <el-card shadow="hover" :class="['schedule-card', { disabled: !schedule.canAppointment }]">
          <div class="date">{{ formatDate(schedule.workDate) }}</div>
          <div class="slot">{{ schedule.timeSlotName }}</div>
          <div class="stock">
            <span :class="schedule.availableCount > 0 ? 'ok' : 'full'">
              {{ schedule.availableCount > 0 ? `余号 ${schedule.availableCount}` : '已约满' }}
            </span>
            <span class="price">¥{{ schedule.price }}</span>
          </div>
          <el-button
            type="primary"
            size="small"
            style="width: 100%"
            :disabled="!schedule.canAppointment"
            @click="openBooking(schedule)"
          >预约</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" title="填写就诊人信息" width="420px">
      <el-form :model="bookingForm" label-width="80px">
        <el-form-item label="就诊人"><el-input v-model="bookingForm.patientName" placeholder="姓名" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="bookingForm.patientPhone" placeholder="手机号" /></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="bookingForm.patientIdCard" placeholder="身份证号" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitBooking">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createAppointment, getDoctorSchedule } from '../api'

const route = useRoute()
const schedules = ref([])
const doctorName = ref('')
const dialogVisible = ref(false)
const submitting = ref(false)
const bookingForm = reactive({ patientName: '', patientPhone: '', patientIdCard: '', scheduleId: null })

function formatDate(d) {
  const date = new Date(d)
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()]
  return `${date.getMonth() + 1}/${date.getDate()} ${week}`
}

async function load() {
  const list = (await getDoctorSchedule(route.params.doctorId)) || []
  schedules.value = list
  doctorName.value = list[0]?.doctorName || ''
}

function openBooking(schedule) {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    return
  }
  bookingForm.scheduleId = schedule.scheduleId
  dialogVisible.value = true
}

async function submitBooking() {
  if (!bookingForm.patientName.trim() || !bookingForm.patientPhone.trim() || !bookingForm.patientIdCard.trim()) {
    ElMessage.warning('请完整填写就诊人信息')
    return
  }
  submitting.value = true
  try {
    await createAppointment({ ...bookingForm, idCardType: 1 })
    ElMessage.success('预约成功，短信通知已发送，请查看我的预约')
    dialogVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.card-col { margin-bottom: 14px; }
.schedule-card { text-align: center; }
.schedule-card.disabled { opacity: 0.55; }
.date { font-weight: 700; }
.slot { color: #666; font-size: 13px; margin: 4px 0; }
.stock { display: flex; justify-content: space-between; margin: 6px 0 10px; font-size: 13px; }
.ok { color: #52c41a; }
.full { color: #999; }
.price { color: #e64a3b; }
</style>
