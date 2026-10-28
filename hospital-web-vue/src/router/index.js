import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/LoginView.vue') },
  { path: '/', component: () => import('../views/HomeView.vue') },
  { path: '/hospital/:hospitalId/departments', component: () => import('../views/DepartmentsView.vue') },
  { path: '/department/:departmentId/doctors', component: () => import('../views/DoctorListView.vue') },
  { path: '/doctor/:doctorId/schedule', component: () => import('../views/ScheduleView.vue') },
  { path: '/appointments', component: () => import('../views/MyAppointmentsView.vue'), meta: { needAuth: true } },
  { path: '/admin', component: () => import('../views/AdminView.vue'), meta: { needAuth: true, admin: true } },
  { path: '/ai', component: () => import('../views/AiTriageView.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.needAuth && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  const user = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (to.meta.admin && user.role !== 'ADMIN') return '/'
  return true
})

export default router
