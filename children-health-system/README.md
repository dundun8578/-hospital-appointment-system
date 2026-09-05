# Web儿童健康帮扶系统

## 项目简介
儿童健康帮扶系统后端服务，提供用户注册、登录等基础功能。

## 技术栈
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- Spring Security
- JWT (java-jwt 4.4.0)
- BCrypt密码加密
- Lombok
- MySQL 8.0+
- Maven

## 功能特性
- ✅ 用户注册（含完整参数校验）
- ✅ 用户登录（JWT Token认证）
- ✅ BCrypt密码加密存储
- ✅ 统一返回格式封装
- ✅ 全局异常处理
- ✅ 跨域配置（支持localhost:12306）
- ✅ JWT Token有效期7天
- ✅ 角色权限管理（家长/医生/管理员）

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 5.7+ 或 8.0+

### 2. 数据库配置
1. 创建数据库：`children_health_system`
2. 执行SQL脚本：`src/main/resources/db/schema.sql`
3. 修改数据库配置：`src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql:<SECRET_58731602>    username: root
    password: 你的MySQL密码
```

### 3. 启动项目

**方法1：IDE运行（推荐）**
- 右键运行 `ChildrenHealthSystemApplication.java`

**方法2：Maven命令**
```bash
mvn spring-boot:run
```

**方法3：打包运行**
```bash
mvn clean package
java -jar target/children-health-system-1.0.0.jar
```

### 4. 访问地址
- 服务地址：http://localhost:12306
- 注册接口：POST http://localhost:12306/api/auth/register
- 登录接口：POST http://localhost:12306/api/auth/login

## API接口文档

### 1. 用户注册
**接口地址：** `POST /api/auth/register`

**请求参数：**
```json
{
    "username": "parent001",
    "password": "123456",
    "email": "parent001@example.com",
    "phone": "13800138000",
    "role": 1
}
```

**参数说明：**
- username: 用户名（3-20位，字母数字下划线）
- password: 密码（6-20位）
- email: 邮箱（可选）
- phone: 手机号（可选）
- role: 角色（1=家长，2=医生，3=管理员，默认1）

**成功响应：**
```json
{
    "code": 200,
    "message": "注册成功",
    "data": {
        "userId": 1,
        "username": "parent001",
        "email": "parent001@example.com",
        "phone": "13800138000",
        "registerTime": "2024-01-15 10:30:00",
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "role": 1,
        "permissions": {
            "child:view": "查看儿童档案",
            "child:edit": "编辑儿童档案",
            ...
        }
    },
    "timestamp": 1705311000000
}
```

### 2. 用户登录
**接口地址：** `POST /api/auth/login`

**请求参数：**
```json
{
    "username": "parent001",
    "password": "123456"
}
```

**成功响应：**
```json
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "userInfo": {
            "id": 1,
            "username": "parent001",
            "email": "parent001@example.com",
            "phone": "13800138000",
            "role": 1,
            "avatar": null
        },
        "permissions": {
            "child:view": "查看儿童档案",
            "child:edit": "编辑儿童档案",
            ...
        }
    },
    "timestamp": 1705311000000
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 40101 | 用户不存在 |
| 40102 | 用户名已存在 |
| 40103 | 邮箱已被注册 |
| 40104 | 手机号已被注册 |
| 40105 | 密码错误 |
| 40106 | 用户已被禁用 |
| 40108 | token无效或已过期 |
| 40301 | 权限不足 |
| 500 | 服务器内部错误 |

## 角色权限说明

### 家长（role=1）
- 查看儿童档案
- 编辑儿童档案
- 健康数据录入
- 查看健康报告
- 发起在线问诊
- 查看问诊记录

### 医生（role=2）
- 查看患者列表
- 管理患者档案
- 健康评估
- 制定健康方案
- 回复问诊咨询
- 查看问诊记录
- 查看健康报告

### 管理员（role=3）
- 用户管理
- 内容审核
- 数据统计
- 系统配置
- 医生管理

## 安全特性
- ✅ BCrypt密码加密（强度10）
- ✅ JWT Token认证（7天有效期）
- ✅ 参数校验（JSR303规范）
- ✅ 全局异常处理
- ✅ 跨域配置
- ✅ SQL注入防护（MyBatis-Plus）
- ✅ XSS防护（Spring Security）

## 注意事项
1. ⚠️ 生产环境需要更换JWT密钥
2. ⚠️ 数据库密码需要修改
3. ⚠️ 需要配置HTTPS
4. ⚠️ 需要添加接口限流
5. ⚠️ 需要添加操作日志

## 许可证
MIT License
