<template>
  <section class="admin-page">
    <div class="page-title">
      <div><h2>排班与号源管理</h2><p>创建医生排班，并将号源同步到 Redis。</p></div>
      <el-button :loading="syncing" @click="syncAll">同步全部号源</el-button>
    </div>
    <el-card>
      <template #header>创建排班</template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="schedule-form">
        <el-form-item label="医生 ID" prop="doctorId"><el-input-number v-model="form.doctorId" :min="1" /></el-form-item>
        <el-form-item label="出诊日期" prop="workDate"><el-date-picker v-model="form.workDate" type="date" value-format="YYYY-MM-DD" :disabled-date="pastDate" /></el-form-item>
        <el-form-item label="出诊时段" prop="timeSlot">
          <el-radio-group v-model="form.timeSlot">
            <el-radio-button :value="1">上午</el-radio-button><el-radio-button :value="2">下午</el-radio-button><el-radio-button :value="3">晚上</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="号源数量" prop="totalCount"><el-input-number v-model="form.totalCount" :min="1" :max="500" /></el-form-item>
        <el-form-item label="挂号费" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item><el-button type="primary" :loading="submitting" @click="submit">创建并预热库存</el-button></el-form-item>
      </el-form>
    </el-card>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createSchedule, syncAllScheduleStock } from '../api'

const formRef = ref()
const submitting = ref(false)
const syncing = ref(false)
const form = reactive({ doctorId: 1, workDate: '', timeSlot: 1, totalCount: 20, price: 0 })
const rules = {
  doctorId: [{ required: true, message: '请输入医生ID', trigger: 'blur' }],
  workDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  totalCount: [{ required: true, message: '请输入号源数量', trigger: 'blur' }]
}
const pastDate = (date) => date.getTime() < new Date().setHours(0, 0, 0, 0)

async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const id = await createSchedule(form)
    ElMessage.success(`排班创建成功，ID：${id}`)
  } finally { submitting.value = false }
}

async function syncAll() {
  syncing.value = true
  try {
    await syncAllScheduleStock()
    ElMessage.success('未来30天号源已同步到Redis')
  } finally { syncing.value = false }
}
</script>

<style scoped>
.admin-page { display: grid; gap: 20px; }
.page-title { display: flex; justify-content: space-between; align-items: center; }
.page-title h2 { margin-bottom: 6px; }
.page-title p { margin: 0; color: #64748b; }
.schedule-form { max-width: 620px; }
</style>
