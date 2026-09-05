# 在线预约挂号平台 - Vue3 前端

技术栈：Vue 3 + Vite + Vue Router + Pinia + Element Plus + Axios

## 页面

| 路由 | 说明 |
|---|---|
| `/login` | 登录 / 注册（JWT） |
| `/` | 医院列表 |
| `/hospital/:hospitalId/departments` | 医院科室 |
| `/department/:departmentId/doctors` | 科室医生 |
| `/doctor/:doctorId/schedule` | 医生未来7天排班 + 预约下单 |
| `/appointments` | 我的预约（取消） |
| `/ai` | **AI 智能导诊**：症状描述 → LLM 推荐科室（基于系统真实科室 grounding） |

## 启动

```bash
# 后端依次启动：user(8082) doctor(8083) appointment(8084) order(8085) ai(8086)
npm install
npm run dev
# 打开 http://localhost:5174
```

Vite 已按路径前缀分别代理到 5 个后端服务（/user→8082、/hospital·department·doctor·schedule→8083、
/appointment→8084、/ai→8086），无需处理跨域。

## AI 导诊演示前提

`hospital-ai/src/main/resources/application.yml` 中 `ai.enabled: true` 并填入真实 API key
（OpenAI 兼容协议：DeepSeek / 智谱 / Kimi / 通义均可）。

演示话术：输入"最近三天头疼低烧" → AI 推荐神经内科等真实科室（系统科室列表已注入 Prompt
做 grounding，幻觉科室会被过滤）；输入"胸痛" → urgency=紧急 并提示拨打 120。

## 核心链路演示（对应简历）

1. 同一账号对同一排班连点"预约" → 第二次被分布式锁+唯一索引拦截（防重复提交）
2. 号源余 1 时两个账号并发抢 → Redis Lua 原子扣减，只成功一个（防超卖）
3. 预约成功 → 订单由 order 服务异步创建 + 模拟短信通知（削峰）
4. 订单 15 分钟未支付 → TTL 延时队列自动关单并回补号源
5. 取消预约 → 号源回补 + 待支付订单联动取消
