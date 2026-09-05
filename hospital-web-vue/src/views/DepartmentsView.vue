<template>
  <div>
    <el-page-header content="选择科室" @back="$router.push('/')" />
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col v-for="department in departments" :key="department.departmentId" :xs="12" :sm="8" :md="6" class="card-col">
        <el-card shadow="hover" class="dept-card" @click="$router.push(`/department/${department.departmentId}/doctors`)">
          <div class="d-name">{{ department.departmentName }}</div>
          <div class="d-desc">{{ department.description || '点击查看医生' }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && departments.length === 0" description="该医院暂无科室" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getDepartments } from '../api'

const route = useRoute()
const departments = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    departments.value = (await getDepartments(route.params.hospitalId)) || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.card-col { margin-bottom: 16px; }
.dept-card { cursor: pointer; }
.d-name { font-weight: 700; margin-bottom: 4px; }
.d-desc { color: #999; font-size: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
</style>
