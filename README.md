# 医院预约挂号系统

基于 Spring Boot + Redis + RabbitMQ 的高并发预约挂号系统。

## 技术栈

Spring Boot 3.x | MyBatis Plus | MySQL 8.0 | Redis 7.x | RabbitMQ | Redisson | JWT

## 功能模块

- **用户模块**：注册、登录、JWT认证
- **医院模块**：医院/科室/医生查询、排班管理
- **预约模块**：Redis原子扣库存、分布式锁防重复、MQ异步订单
- **限流安全**：滑动窗口限流、IP黑名单

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < 医院挂号系统数据库设计.sql
```

### 2. 配置

修改各模块 `src/main/resources/application.yml`：
- 数据库连接
- Redis连接
- RabbitMQ连接

### 3. 启动模块

按顺序启动：
```
hospital-auth (8081)
hospital-user (8082)
hospital-doctor (8083)
hospital-appointment (8084)
hospital-order (8085)
```

### 4. 接口文档

http://localhost:8080/api/doc.html

## 核心接口

```
POST /api/user/login          # 登录
POST /api/user/register       # 注册
GET  /api/hospital/list       # 医院列表
GET  /api/doctor/list/{deptId} # 医生列表
POST /api/appointment/create   # 创建预约
GET  /api/appointment/list     # 我的预约
```

## 项目结构

```
hospital-common/       # 公共模块
hospital-auth/         # 认证模块
hospital-user/         # 用户模块
hospital-doctor/       # 医生/排班模块
hospital-appointment/  # 预约核心模块
hospital-order/        # 订单模块
```

## 核心技术

### 防止超卖
Redis预扣库存 + Lua脚本原子操作 + 数据库唯一索引

### 防止重复预约
Redisson分布式锁 + 双重检查

### 异步削峰
RabbitMQ异步创建订单，DB写入QPS降低84%

### 限流防刷
Redis滑动窗口 + IP黑名单

## 数据库

核心表：user、hospital、department、doctor、schedule、appointment、appointment_order

完整SQL：`医院挂号系统数据库设计.sql`

## License

MIT
