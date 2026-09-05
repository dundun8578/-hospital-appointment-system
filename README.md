# 医院预约挂号系统

基于 Spring Boot 3.2 + Redis + RabbitMQ + Redisson 的高并发预约挂号系统（模块化单体架构）。

## 技术栈

Spring Boot 3.2.5 (Java 17) | MyBatis Plus | MySQL 8.0 | Redis | RabbitMQ | Redisson | JWT

## 模块说明

| 模块 | 职责 | 端口 |
|---|---|---|
| `hospital-common` | 公共库：无状态JWT工具与登录拦截器、限流/IP黑名单/请求日志拦截器、统一异常、安全配置 | - |
| `hospital-user` | 用户服务：注册、登录（签发JWT）、用户信息 | 8082 |
| `hospital-doctor` | 医院服务：医院/科室/医生查询、排班管理、号源库存同步 | 8083 |
| `hospital-appointment` | 预约服务（核心）：分布式锁防重复、Redis原子扣库存、DB扣减同步、发送MQ消息 | 8084 |
| `hospital-order` | 订单服务：监听预约队列异步创建订单，失败消息进入死信队列 | 8085 |

## 核心链路：预约挂号

```
用户预约 → 分布式锁(userId+scheduleId，防重复提交)
        → DB双重检查是否已预约
        → Redis DECR 原子扣减号源（扣成负数自动回补，防并发超挂）
        → 落库 appointment + 扣减 schedule.available_count（失败回补Redis）
        → 发送MQ消息 → 订单服务异步创建订单
        → 消费失败(重试3次后) → 死信交换机 → appointment.dlq 供排查重放
取消预约 → 校验归属与状态 → Redis回补库存 + DB号源回滚
        → 联动取消待支付订单（MQ通知订单服务）
```

## 可靠性设计

- **消费幂等**：appointment_order 表 appointment_id 唯一索引，重复消费捕获冲突返回既有订单
- **延时关单**：订单创建后写入 TTL 15 分钟延时队列（队列级TTL防队头阻塞），到期死信进入
  order.cancel.queue，仍未支付则自动取消订单并回补号源；已支付/已取消则幂等跳过
- **取消联动**：用户取消预约通过 MQ 通知订单服务同步取消待支付订单（号源只回补一次）
- **消息可靠性**：交换机/队列/消息持久化，消费重试耗尽后不重入队直接进死信

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < 医院挂号系统数据库设计.sql
```

### 2. 配置

各模块 `src/main/resources/application.yml`：数据库/Redis/RabbitMQ 连接（默认本机）。

### 3. 构建

```bash
mvn clean package -DskipTests
# 每个服务产出两个jar：-exec.jar 为可运行版本（普通jar供模块间依赖编译）
```

### 4. 启动（按顺序）

```bash
java -jar hospital-user/target/hospital-user-1.0.0-exec.jar         # 8082 注册/登录
java -jar hospital-doctor/target/hospital-doctor-1.0.0-exec.jar     # 8083 医院/科室/医生/排班
java -jar hospital-appointment/target/hospital-appointment-1.0.0-exec.jar  # 8084 预约挂号
java -jar hospital-order/target/hospital-order-1.0.0-exec.jar       # 8085 异步订单
```

> 登录校验由各服务装载的 `JwtInterceptor`（common 模块）无状态完成；
> 系统启动时 `StockInitializer` 会把全部排班号源预热进 Redis。

## 接口文档

各服务启动后访问 `http://localhost:{port}/doc.html`（Knife4j）。
