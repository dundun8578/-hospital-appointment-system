<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">在线预约挂号平台</h2>
      <el-tabs v-model="tab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-width="0">
            <el-form-item>
              <el-input v-model="loginForm.username" placeholder="用户名" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password />
            </el-form-item>
            <el-button type="primary" style="width: 100%" :loading="loading" @click="doLogin">登 录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-width="0">
            <el-form-item><el-input v-model="registerForm.username" placeholder="用户名" /></el-form-item>
            <el-form-item><el-input v-model="registerForm.password" type="password" placeholder="密码" show-password /></el-form-item>
            <el-form-item><el-input v-model="registerForm.realName" placeholder="真实姓名" /></el-form-item>
            <el-form-item><el-input v-model="registerForm.phone" placeholder="手机号" /></el-form-item>
            <el-form-item><el-input v-model="registerForm.idCard" placeholder="身份证号" /></el-form-item>
            <el-button type="primary" style="width: 100%" :loading="loading" @click="doRegister">注 册</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api'

const route = useRoute()
const router = useRouter()
const tab = ref('login')
const loading = ref(false)
const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', password: '', realName: '', phone: '', idCard: '' })

function saveTokenAndGo(data) {
  localStorage.setItem('token', data.token)
  localStorage.setItem('userInfo', JSON.stringify(data.userInfo || {}))
  router.push(route.query.redirect || '/')
}

async function doLogin() {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    saveTokenAndGo(await login(loginForm.value))
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  if (!registerForm.value.username || !registerForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    saveTokenAndGo(await register(registerForm.value))
    ElMessage.success('注册成功，已自动登录')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { display: flex; justify-content: center; padding-top: 70px; }
.login-card { width: 400px; }
.title { text-align: center; color: #1677ff; margin: 8px 0 16px; }
</style>
