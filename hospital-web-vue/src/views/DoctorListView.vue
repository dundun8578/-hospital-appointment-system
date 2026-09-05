<template>
  <div>
    <el-page-header content="科室医生" @back="$router.back()" />
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col v-for="doctor in doctors" :key="doctor.doctorId" :xs="24" :sm="12" class="card-col">
        <el-card shadow="hover" class="doctor-card" @click="$router.push(`/doctor/${doctor.doctorId}/schedule`)">
          <div class="row">
            <div>
              <div class="doc-name">{{ doctor.doctorName }} <el-tag size="small">{{ doctor.title || '医师' }}</el-tag></div>
              <div class="doc-spec">{{ doctor.specialty || '擅长各常见病诊治' }}</div>
            </div>
            <div class="doc-price">¥{{ doctor.price }} <span class="next">最近可约: {{ doctor.nextAvailableDate || '暂无' }}</span></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && doctors.length === 0" description="该科室暂无医生" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getDoctors } from '../api'

const route = useRoute()
const doctors = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    doctors.value = (await getDoctors(route.params.departmentId)) || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.card-col { margin-bottom: 16px; }
.doctor-card { cursor: pointer; }
.row { display: flex; justify-content: space-between; align-items: center; gap: 8px; }
.doc-name { font-weight: 700; margin-bottom: 4px; }
.doc-spec { color: #999; font-size: 12px; }
.doc-price { color: #e64a3b; font-weight: 700; white-space: nowrap; }
.next { display: block; color: #999; font-size: 12px; font-weight: 400; text-align: right; }
</style>
