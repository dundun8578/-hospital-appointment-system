<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-inner">
        <span class="logo" @click="$router.push('/')">🏥 在线预约挂号平台</span>
        <div class="actions">
          <el-button link @click="$router.push('/ai')">AI导诊</el-button>
          <el-button v-if="token" link @click="$router.push('/appointments')">我的预约</el-button>
          <el-button v-if="!token" type="primary" plain @click="$router.push('/login')">登录</el-button>
          <el-button v-else link @click="logout">{{ nickname }} · 退出</el-button>
        </div>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const token = ref(localStorage.getItem('token'))
const nickname = ref(JSON.parse(localStorage.getItem('userInfo') || '{}').realName || '用户')

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  token.value = null
  router.push('/login')
}
</script>

<style scoped>
.layout { min-height: 100vh; background: #f4f8fb; }
.header { background: #fff; border-bottom: 1px solid #e5eef5; position: sticky; top: 0; z-index: 10; }
.header-inner { max-width: 1100px; margin: 0 auto; display: flex; align-items: center; height: 60px; }
.logo { font-size: 19px; font-weight: bold; color: #1677ff; cursor: pointer; }
.actions { margin-left: auto; display: flex; align-items: center; }
.main { max-width: 1100px; margin: 0 auto; width: 100%; }
</style>
