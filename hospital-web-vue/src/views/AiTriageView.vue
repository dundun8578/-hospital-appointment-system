<template>
  <div class="ai-page">
    <el-card shadow="never">
      <template #header><span class="ai-title">✨ AI 智能导诊</span></template>
      <el-input
        v-model="symptoms"
        type="textarea"
        :rows="4"
        maxlength="300"
        show-word-limit
        placeholder="描述您的症状，例如：最近三天头疼，伴有低烧，咳嗽时加重..."
      />
      <el-button
        type="primary"
        style="margin-top: 12px"
        :loading="loading"
        @click="doTriage"
      >AI 分析推荐科室</el-button>
      <div class="tip">分析结果仅供参考，如有急症请立即前往急诊或拨打120</div>
    </el-card>

    <template v-if="result">
      <el-alert
        v-if="result.fallback"
        type="warning"
        :title="result.advice"
        show-icon
        style="margin-top: 16px"
      />
      <el-card v-else shadow="never" style="margin-top: 16px">
        <h4>推荐科室</h4>
        <el-card
          v-for="rec in result.recommendations"
          :key="rec.departmentId"
          shadow="hover"
          class="rec-card"
          @click="gotoDepartment(rec.departmentId)"
        >
          <div class="rec-row">
            <div>
              <span class="rec-name">{{ rec.departmentName }}</span>
              <el-tag :type="urgencyType(rec.urgency)" size="small" style="margin-left: 8px">{{ rec.urgency }}</el-tag>
              <div class="rec-reason">{{ rec.reason }}</div>
            </div>
            <el-button size="small" type="primary" plain>去挂号</el-button>
          </div>
        </el-card>
        <el-alert type="info" :title="result.advice" :closable="false" style="margin-top: 12px" />
      </el-card>
      <div class="disclaimer">{{ result.disclaimer }}</div>
    </template>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { aiTriage } from '../api'

const router = useRouter()
const symptoms = ref('')
const result = ref(null)
const loading = ref(false)

async function doTriage() {
  if (!symptoms.value.trim()) {
    ElMessage.warning('请描述您的症状')
    return
  }
  loading.value = true
  try {
    result.value = await aiTriage(symptoms.value.trim())
  } finally {
    loading.value = false
  }
}

function urgencyType(urgency) {
  return ({ '紧急': 'danger', '尽快': 'warning', '常规': 'info' })[urgency] || 'info'
}

function gotoDepartment(departmentId) {
  router.push(`/department/${departmentId}/doctors`)
}
</script>

<style scoped>
.ai-page { max-width: 720px; margin: 0 auto; }
.ai-title { font-weight: 700; background: linear-gradient(90deg,#7c4dff,#1677ff); -webkit-background-clip: text; color: transparent; }
.tip { color: #bbb; font-size: 12px; margin-top: 8px; }
.rec-card { cursor: pointer; margin-bottom: 10px; }
.rec-row { display: flex; justify-content: space-between; align-items: center; }
.rec-name { font-weight: 700; }
.rec-reason { color: #999; font-size: 13px; margin-top: 4px; }
.disclaimer { color: #bbb; font-size: 12px; text-align: center; margin-top: 12px; }
</style>
