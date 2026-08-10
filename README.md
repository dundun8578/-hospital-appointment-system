# 医院预约挂号系统

基于Spring Boot + Redis + RabbitMQ的高并发预约挂号系统，支持500+并发用户同时抢号。

## 技术栈

- **后端框架**：Spring Boot 3.x + MyBatis Plus
- **数据库**：MySQL 8.0
- **缓存**：Redis 7.x
- **消息队列**：RabbitMQ
- **分布式锁**：Redisson
- **认证**：JWT
- **压测**：JMeter 5.5
- **API文档**：Knife4j

## 核心功能

### 用户模块
- 用户注册/登录
- JWT Token认证
- 个人信息管理

### 医院模块
- 医院/科室/医生查询
- 医生排班管理
- 号源库存管理

### 预约挂号
- Redis + Lua原子扣减库存
- 分布式锁防重复预约
- RabbitMQ异步创建订单
- 限流防刷

## 快速开始

### 1. 环境要求

- JDK 17
- Maven 3.8+
- MySQL 8.0
- Redis 7.x
- RabbitMQ 3.12

### 2. 数据库初始化

```bash
mysql -u root -p < 医院挂号系统数据库设计.sql
```

### 3. 修改配置

编辑各模块的 `application.yml`：
- 数据库连接（MySQL）
- Redis连接
- RabbitMQ连接

### 4. 启动项目

按顺序启动5个模块：
```bash
# hospital-auth (8081)
# hospital-user (8082)
# hospital-doctor (8083)
# hospital-appointment (8084)
# hospital-order (8085)
```

### 5. 访问文档

http://localhost:8080/api/doc.html

## 项目结构

```
hospital-appointment-system/
├── hospital-common/        # 公共模块
├── hospital-auth/          # 认证模块
├── hospital-user/          # 用户模块
├── hospital-doctor/        # 医生/排班模块
├── hospital-appointment/   # 预约核心模块
├── hospital-order/         # 订单模块
└── jmeter/               # 压测脚本
```

## API接口

### 用户
- `POST /api/user/register` - 注册
- `POST /api/user/login` - 登录
- `GET /api/user/info` - 获取用户信息

### 医院
- `GET /api/hospital/list` - 医院列表
- `GET /api/hospital/{id}` - 医院详情

### 医生
- `GET /api/doctor/list/{departmentId}` - 医生列表
- `GET /api/doctor/{id}` - 医生详情

### 预约
- `POST /api/appointment/create` - 创建预约
- `GET /api/appointment/list` - 我的预约

## 性能指标

| 指标 | 结果 |
|------|------|
| 并发用户数 | 500 |
| QPS | 500+ |
| P99响应时间 | <350ms |
| 成功率 | >99% |
| 超卖问题 | 已解决 |
| 重复预约 | 已解决 |

## 核心技术实现

### 1. 防止超卖
- Redis预扣库存 + Lua原子操作
- 数据库唯一索引兜底

### 2. 防止重复预约
- Redisson分布式锁
- 双重检查机制

### 3. 异步削峰
- RabbitMQ异步创建订单
- DB写入QPS降低84%

### 4. 限流防刷
- Redis滑动窗口
- IP黑名单

## 数据库设计

### 核心表
- user - 用户表
- hospital - 医院表
- department - 科室表
- doctor - 医生表
- schedule - 排班表（库存）
- appointment - 预约表
- appointment_order - 订单表

详见：`医院挂号系统数据库设计.sql`

## 压测

```bash
jmeter -n -t jmeter/scripts/appointment_test_plan.jmx -l result.jtl
```

压测脚本：`jmeter/scripts/appointment_test_plan.jmx`

## License

MIT
