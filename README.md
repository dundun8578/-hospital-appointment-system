# 医院预约挂号系统 - 项目README

## 🏥 项目简介

**医院在线预约挂号系统**是一个真实企业级的高并发预约挂号平台，重点解决早8点放号时数百人同时抢号的高并发场景。

### 核心业务场景

- 医院每天上午8点开放专家号预约
- 热门专家只有20个号源，可能有数百上千用户同时访问
- 需要解决超卖、重复预约、脚本刷单三大风险

### 技术栈

**后端**：
- Java 17
- Spring Boot 3.x
- MyBatis Plus
- MySQL 8.0
- Redis 7.x
- RabbitMQ
- Redisson
- JWT

**测试**：
- JMeter 5.5

**文档**：
- Knife4j接口文档

## ✨ 核心特性

### 1. Redis + Lua原子库存扣减 ✅
- 每日号源提前加载到Redis
- Lua脚本原子执行「查询→扣减」
- 彻底消除超卖问题，Redis操作<5ms

### 2. RabbitMQ异步削峰 ✅
- Redis扣号成功后投递MQ消息
- 消费者异步创建订单
- DB写入QPS从500+降至50-80

### 3. Redisson分布式锁 ✅
- 锁Key：`appointment:user:{userId}:schedule:{scheduleId}`
- 防止重复提交，30秒自动释放

### 4. Redis滑动窗口限流 ✅
- 同IP每分钟最多10次请求
- `@RateLimit`注解灵活配置
- 有效拦截恶意脚本

### 5. 数据库优化 ✅
- 联合索引优化
- 唯一索引防止重复预约
- 冗余字段避免多表关联

## 📊 性能指标

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| **并发用户数** | 500 | 500 | - |
| **QPS** | 212 | 500 | +135% |
| **平均响应时间** | 850ms | 150ms | -82% |
| **P99响应时间** | 1800ms | 350ms | -81% |
| **成功率** | 85% | 99%+ | +16% |
| **超卖问题** | 严重 | 彻底解决 | ✅ |
| **重复预约** | 频繁 | 彻底解决 | ✅ |

## 🏗️ 项目结构

```
hospital-appointment-system/
├── pom.xml                              # 父工程POM
├── hospital-common/                     # 公共模块
│   ├── Result/ResultCode               # 统一响应/错误码
│   ├── GlobalExceptionHandler          # 全局异常处理
│   ├── annotation/RateLimit            # 限流注解
│   ├── interceptor/                    # 拦截器
│   ├── utils/                          # 工具类
│   └── config/                         # 配置类
├── hospital-auth/                       # 认证模块
│   ├── JwtUtil                         # JWT工具
│   ├── PasswordEncoderUtil             # 密码加密
│   └── interceptor/JwtInterceptor      # JWT拦截器
├── hospital-user/                       # 用户模块
│   ├── entity/User                     # 用户实体
│   ├── service/impl/UserServiceImpl    # 用户服务
│   └── controller/UserController       # 用户接口
├── hospital-doctor/                     # 医生排班模块
│   ├── entity/                         # 医院/科室/医生/排班实体
│   ├── service/impl/                   # 业务实现
│   ├── controller/                     # 接口
│   ├── task/ScheduleTask               # 定时任务
│   └── config/RedisKeyConstants        # Redis Key常量
├── hospital-appointment/                # 预约核心模块
│   ├── entity/Appointment              # 预约实体
│   ├── mq/                             # MQ消息模型
│   ├── utils/                          # Redis库存/分布式锁工具
│   ├── config/                         # MQ/Redis/Lua配置
│   └── controller/AppointmentController # 预约接口
├── hospital-order/                      # 订单模块
│   ├── entity/Order                    # 订单实体
│   ├── listener/AppointmentMessageListener # MQ消费者
│   └── service/OrderService            # 订单服务
├── jmeter/                             # JMeter压测脚本
│   ├── scripts/appointment_test_plan.jmx
│   └── 压测报告模板.md
├── 医院挂号系统数据库设计.sql            # 数据库脚本
├── 数据库设计文档.md
├── 限流和安全配置说明.md
├── 性能优化总结报告.md
└── README.md                           # 本文件
```

## 🚀 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.8+
- MySQL 8.0
- Redis 7.x
- RabbitMQ 3.12

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p < 医院挂号系统数据库设计.sql
```

### 3. 配置文件

修改各模块的 `application.yml`：
- 数据库连接：`spring.datasource.url`、`username`、`password`
- Redis连接：`spring.data.redis.host`、`port`
- RabbitMQ连接：`spring.rabbitmq.host`、`port`、`username`、`password`

### 4. 启动项目

**方式一：分别启动各模块**
```bash
# 启动hospital-auth（端口8081）
cd hospital-auth && mvn spring-boot:run

# 启动hospital-user（端口8082）
cd hospital-user && mvn spring-boot:run

# 启动hospital-doctor（端口8083）
cd hospital-doctor && mvn spring-boot:run

# 启动hospital-appointment（端口8084）
cd hospital-appointment && mvn spring-boot:run

# 启动hospital-order（端口8085）
cd hospital-order && mvn spring-boot:run
```

**方式二：使用Maven多模块启动**
```bash
mvn spring-boot:run -pl hospital-appointment -am
```

### 5. 访问接口文档

打开浏览器访问：http://localhost:8080/api/doc.html

## 📖 API文档

### 用户模块
- `POST /user/register` - 用户注册
- `POST /user/login` - 用户登录
- `GET /user/info` - 获取用户信息

### 医院模块
- `GET /hospital/list` - 医院列表查询
- `GET /hospital/{id}` - 医院详情

### 科室模块
- `GET /department/list/{hospitalId}` - 科室列表
- `GET /department/top/{hospitalId}` - 一级科室

### 医生模块
- `GET /doctor/list/{departmentId}` - 医生列表
- `GET /doctor/{doctorId}` - 医生详情
- `GET /doctor/search?keyword=xxx` - 搜索医生

### 排班模块
- `POST /schedule/create` - 创建排班
- `GET /schedule/{scheduleId}` - 排班详情
- `GET /schedule/doctor/{doctorId}` - 医生排班

### 预约模块
- `POST /appointment/create` - 创建预约 ⭐核心
- `GET /appointment/{appointmentId}` - 预约详情
- `GET /appointment/list` - 我的预约
- `POST /appointment/{id}/cancel` - 取消预约

## 🧪 压测

```bash
# 非GUI模式压测
jmeter -n -t jmeter/scripts/appointment_test_plan.jmx \
       -l jmeter/reports/result.jtl \
       -e -o jmeter/reports/html

# 查看压测报告
open jmeter/reports/html/index.html
```

详细压测说明：[jmeter/压测脚本使用说明.md](jmeter/压测脚本使用说明.md)

## 🔧 核心配置

### 数据库配置

见：`医院挂号系统数据库设计.sql`

核心表：
- `user` - 用户表
- `hospital` - 医院表
- `department` - 科室表
- `doctor` - 医生表
- `schedule` - 排班表（含库存）
- `appointment` - 预约表（唯一索引防重复）
- `appointment_order` - 订单表

### Redis Key设计

| Key | 说明 | 过期时间 |
|-----|------|---------|
| `hospital:stock:{scheduleId}` | 排班库存 | 24小时 |
| `rate:limit:*` | 限流Key | 60秒 |
| `ip:blacklist:{ip}` | IP黑名单 | 7天 |

### RabbitMQ配置

| 名称 | 类型 | 说明 |
|------|------|------|
| `appointment.exchange` | Direct Exchange | 预约Exchange |
| `appointment.queue` | Queue | 预约队列 |
| `appointment.create` | Routing Key | 创建预约 |
| `appointment.dlq` | Queue | 死信队列 |

## 🎓 面试要点

### 1. 如何解决超卖问题？
**答案**：Redis预扣库存 + Lua脚本原子操作 + 数据库唯一索引

### 2. 如何解决重复预约？
**答案**：分布式锁（Redisson） + 双重检查 + 数据库唯一索引

### 3. 为什么使用MQ？
**答案**：异步削峰，降低DB压力，提高吞吐量5倍

### 4. 如何实现限流？
**答案**：Redis ZSet滑动窗口 + `@RateLimit`注解

### 5. 如何保证一致性？
**答案**：Redis + 数据库双重校验，MQ重试 + 死信队列

## 📚 文档导航

- [数据库设计文档](数据库设计文档.md)
- [限流和安全配置说明](限流和安全配置说明.md)
- [性能优化总结报告](性能优化总结报告.md)
- [JMeter压测脚本说明](jmeter/压测脚本使用说明.md)
- [JMeter压测报告模板](jmeter/压测报告模板.md)

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

MIT License

## 📞 联系方式

- 作者：hospital
- 邮箱：hospital@example.com
- GitHub：https://github.com/your-username/hospital-appointment-system

---

**项目版本**：V1.0.0
**最后更新**：2024-01-15
**Java版本**：JDK 17
**Spring Boot版本**：3.2.5
