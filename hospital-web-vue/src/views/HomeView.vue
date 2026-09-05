<template>
  <div>
    <h3>选择医院</h3>
    <el-row :gutter="16">
      <el-col v-for="hospital in hospitals" :key="hospital.hospitalId" :xs="24" :sm="12" :md="8" class="card-col">
        <el-card shadow="hover" class="hospital-card" @click="$router.push(`/hospital/${hospital.hospitalId}/departments`)">
          <div class="h-name">{{ hospital.hospitalName }}</div>
          <div class="h-level"><el-tag size="small" effect="plain">{{ hospital.level || '综合医院' }}</el-tag></div>
          <div class="h-meta">科室 {{ hospital.departmentCount || 0 }} · 医生 {{ hospital.doctorCount || 0 }}</div>
          <div class="h-address">{{ hospital.address }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && hospitals.length === 0" description="暂无医院信息" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getHospitals } from '../api'

const hospitals = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    hospitals.value = (await getHospitals()) || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.card-col { margin-bottom: 16px; }
.hospital-card { cursor: pointer; }
.h-name { font-weight: 700; font-size: 16px; margin-bottom: 6px; }
.h-meta { color: #1677ff; font-size: 13px; margin: 4px 0; }
.h-address { color: #999; font-size: 12px; }
</style>
