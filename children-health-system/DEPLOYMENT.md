# 快速部署指南

## ✅ 项目已创建完成

项目路径：`D:\ACLOS\blog-backend\children-health-system`

## 📋 前置检查（已通过）

- ✅ Maven编译成功（15个源文件）
- ✅ 项目结构完整
- ✅ 端口配置：12306
- ✅ 所有依赖配置正确

## 🔧 在您的本地Windows环境运行

### 第一步：安装MySQL数据库

1. **下载MySQL**
   - 访问：https://dev.mysql.com/downloads/mysql/
   - 下载 Windows (x86, 64-bit), ZIP Archive 版本
   - 或使用安装包：https://dev.mysql.com/downloads/installer/

2. **安装MySQL**
   - 解压到：`C:\mysql-8.0.34-winx64`
   - 或运行安装程序，一路"下一步"

3. **初始化MySQL**
   ```cmd
   cd C:\mysql-8.0.34-winx64\bin
   mysqld --initialize --console
   ```

4. **启动MySQL服务**
   ```cmd
   mysqld --install MySQL80
   net start MySQL80
   ```

### 第二步：创建数据库

1. 打开命令提示符（Win+R → cmd）

2. 登录MySQL：
   ```cmd
   cd C:\mysql-8.0.34-winx64\bin
   mysql -u root -p
   ```

3. 执行建表SQL：
   ```sql
   CREATE DATABASE IF NOT EXISTS children_health_system
       DEFAULT CHARACTER SET utf8mb4
       DEFAULT COLLATE utf8mb4_unicode_ci;

   USE children_health_system;

   -- 复制 schema.sql 的内容执行
   -- 文件路径：D:\ACLOS\blog-backend\children-health-system\src\main\resources\db\schema.sql
   ```

   或直接执行命令：
   ```cmd
   mysql -u root -p children_health_system < "D:\ACLOS\blog-backend\children-health-system\src\main\resources\db\schema.sql"
   ```

### 第三步：配置数据库连接

编辑文件：`D:\ACLOS\blog-backend\children-health-system\src\main\resources\application.yml`

修改第11-13行数据库配置：
```yaml
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql:<SECRET_58731602>    username: root
    password: 你的MySQL密码  # 修改这里
```

### 第四步：启动项目

**方法1：IDE运行（推荐）**
1. 用IntelliJ IDEA或Eclipse打开项目文件夹
2. 找到 `ChildrenHealthSystemApplication.java`
3. 右键 → Run 'ChildrenHealthSystemApplication'
4. 等待启动成功

**方法2：Maven命令**
```cmd
cd D:\ACLOS\blog-backend\children-health-system
mvn spring-boot:run
```

**方法3：打包运行**
```cmd
cd D:\ACLOS\blog-backend\children-health-system
mvn clean package
java -jar target/children-health-system-1.0.0.jar
```

### 第五步：验证启动成功

启动成功后，控制台会显示：
```
========================================
儿童健康帮扶系统启动成功！
访问地址：http://localhost:12306
注册接口：POST http://localhost:12306/api/auth/register
登录接口：POST http://localhost:12306/api/auth/login
========================================
```

## 🧪 Postman接口测试

### 测试1：用户注册

**请求配置：**
- 方法：`POST`
- URL：`http://localhost:12306/api/auth/register`
- Headers：`Content-Type: application/json`
- Body（raw JSON）：
```json
{
    "username": "parent001",
    "password": "123456",
    "email": "parent001@example.com",
    "phone": "13800138000",
    "role": 1
}
```

**预期响应（200）：**
```json
{
    "code": 200,
    "message": "注册成功",
    "data": {
        "userId": 1,
        "username": "parent001",
        "email": "parent001@example.com",
        "phone": "13800138000",
        "registerTime": "2024-08-16T14:30:00",
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "role": 1,
        "permissions": {
            "child:view": "查看儿童档案",
            "child:edit": "编辑儿童档案",
            "health:record": "健康数据录入",
            "health:view": "查看健康报告",
            "consultation:create": "发起在线问诊",
            "consultation:view": "查看问诊记录"
        }
    },
    "timestamp": 1723828200000
}
```

---

### 测试2：用户登录

**请求配置：**
- 方法：`POST`
- URL：`http://localhost:12306/api/auth/login`
- Headers：`Content-Type: application/json`
- Body（raw JSON）：
```json
{
    "username": "parent001",
    "password": "123456"
}
```

**预期响应（200）：**
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
            "health:record": "健康数据录入",
            "health:view": "查看健康报告",
            "consultation:create": "发起在线问诊",
            "consultation:view": "查看问诊记录"
        }
    },
    "timestamp": 1723828200000
}
```

---

## 🚨 常见问题解决

### 问题1：端口被占用

**错误信息：**
```
Port 12306 is already in use
```

**解决方法：**
```cmd
# 查找占用端口的进程
netstat -ano | findstr :12306

# 假设PID是12345，结束进程
taskkill /PID 12345 /F
```

### 问题2：MySQL连接失败

**错误信息：**
```
Connection refused to database
```

**检查步骤：**
1. MySQL服务是否启动：
   ```cmd
   net start MySQL80
   ```
2. 数据库是否创建：
   ```cmd
   mysql -u root -p -e "SHOW DATABASES;"
   ```
3. 账号密码是否正确

### 问题3：编译失败

**解决方法：**
```cmd
# 清理并重新编译
mvn clean compile -U
```

### 问题4：找不到MySQL驱动

**错误信息：**
```
ClassNotFoundException: com.mysql.cj.jdbc.Driver
```

**解决方法：**
```cmd
# 重新下载依赖
mvn dependency:purge-local-repository
mvn clean install
```

### 问题5：Bean创建失败

**检查：**
1. 确保数据库表已创建
2. 检查application.yml配置是否正确
3. 查看日志中的具体错误信息

## 📊 项目统计

- **总文件数：** 18个
- **Java源文件：** 15个
- **配置文件：** 2个（application.yml, pom.xml）
- **SQL脚本：** 1个
- **总代码行数：** 约1500行

## ✅ 项目完成度

| 模块 | 完成度 | 说明 |
|------|--------|------|
| 项目结构 | 100% | 标准Spring Boot项目 |
| 实体类 | 100% | User.java完整实现 |
| Mapper层 | 100% | UserMapper继承BaseMapper |
| Service层 | 100% | 接口+实现类完整 |
| Controller层 | 100% | 注册+登录接口 |
| 统一返回 | 100% | Result<T>泛型封装 |
| 异常处理 | 100% | 全局异常处理器 |
| JWT认证 | 100% | Token生成+验证 |
| 密码加密 | 100% | BCrypt加密 |
| 跨域配置 | 100% | 支持localhost:12306 |
| 端口配置 | 100% | server.port=12306 |

## 🎯 下一步

1. 安装MySQL数据库
2. 执行schema.sql创建表
3. 修改数据库密码配置
4. 启动项目
5. 使用Postman测试接口

祝您笔试顺利！💪
