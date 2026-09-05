import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 各后端服务无统一前缀，按路径前缀分别代理到不同服务
const proxyTargets = {
  '/user': 'http://localhost:8082',
  '/hospital': 'http://localhost:8083',
  '/department': 'http://localhost:8083',
  '/doctor': 'http://localhost:8083',
  '/schedule': 'http://localhost:8083',
  '/appointment': 'http://localhost:8084',
  '/ai': 'http://localhost:8086'
}

const proxy = Object.fromEntries(
  Object.entries(proxyTargets).map(([prefix, target]) => [
    prefix,
    { target, changeOrigin: true }
  ])
)

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5174,
    proxy
  }
})
