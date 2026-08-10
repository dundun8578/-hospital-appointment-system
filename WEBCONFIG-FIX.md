# WebConfig 爆红问题修复总结

## 问题根源

`WebConfig.java` 第 3 行引用了一个**不存在的类**：
```java
import com.blog.interceptor.JwtInterceptor;  // ❌ 这个类不存在
```

## 已修复的问题

### 1. ✅ 创建了 JwtInterceptor（JWT 拦截器）
**位置：** `src/main/java/com/itheima/blogbackend/interceptor/JwtInterceptor.java`

**功能：**
- 验证请求头中的 JWT Token
- 自动处理 OPTIONS 预检请求
- 从 Token 中提取用户信息并存入 request
- 统一异常处理（未登录、Token 无效等）

### 2. ✅ 创建了 JwtUtils（JWT 工具类）
**位置：** `src/main/java/com/itheima/blogbackend/utils/JwtUtils.java`

**功能：**
- 生成 JWT Token
- 解析 Token
- 获取用户ID和用户名
- 验证 Token 是否过期
- 密钥长度自动填充（兼容短密钥）

### 3. ✅ 更新了 BusinessException
**位置：** `src/main/java/com/itheima/blogbackend/common/BusinessException.java`

**改动：**
- 增加了 `code` 字段支持
- 支持自定义错误码（如 401、403、500 等）
- 提供了两个构造方法：
  - `BusinessException(String message)` - 默认 500 错误
  - `BusinessException(Integer code, String message)` - 自定义错误码

### 4. ✅ 更新了 GlobalExceptionHandler
**位置：** `src/main/java/com/itheima/blogbackend/common/GlobalExceptionHandler.java`

**改动：**
- 使用 `e.getCode()` 返回自定义错误码
- 支持 JWT 拦截器抛出的 401 等状态码

### 5. ✅ 修复了 WebConfig 的 import 路径
**改动：**
```java
// ❌ 旧的错误路径
import com.blog.interceptor.JwtInterceptor;

// ✅ 新的正确路径
import com.itheima.blogbackend.interceptor.JwtInterceptor;
```

### 6. ✅ 修复了 application.yml 配置
**位置：** `src/main/resources/application.yml`

**改动：**
- 删除了不兼容的 `log-impl` 配置
- 改用 Spring Boot 标准的 `logging.level` 配置

## 当前项目结构

```
src/main/java/com/itheima/blogbackend/
├── BlogBackendApplication.java          # 主启动类
├── common/                              # 通用类
│   ├── BusinessException.java           # ✅ 已更新
│   ├── GlobalExceptionHandler.java      # ✅ 已更新
│   └── Result.java                      # 响应结果封装
├── config/                              # 配置类
│   ├── MybatisPlusConfig.java           # MyBatis-Plus 配置
│   └── WebConfig.java                   # ✅ 已修复
├── entity/                              # 实体类
│   └── User.java                        # 用户实体
├── interceptor/                         # 拦截器
│   └── JwtInterceptor.java              # ✅ 新增
├── mapper/                              # 数据访问层
│   └── UserMapper.java
├── service/                             # 业务逻辑层
│   └── UserService.java
└── utils/                               # 工具类
    └── JwtUtils.java                    # ✅ 新增
```

## 后续使用

### 登录接口示例
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO loginDTO) {
        // 验证用户名密码
        User user = userService.getByUsername(loginDTO.getUsername());
        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        // 生成 Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        return Result.success(token);
    }
}
```

### 需要登录的接口
在 `WebConfig.java` 中已配置：
```java
.addPathPatterns("/api/admin/**")        // 需要拦截的路径
.excludePathPatterns("/api/auth/**")     // 排除的路径（登录接口）
```

### 请求时携带 Token
```
Authorization: Bearer <your-token>
```

## 配置说明

**application.yml 中的 JWT 配置：**
```yaml
blog:
  jwt:
    secret: blog-graduation-project-secret-key-2026
    expiration: 86400000  # 24小时（毫秒）
```

**建议：**
- 生产环境更换更复杂的 secret
- secret 长度建议至少 32 位
- expiration 根据需求调整（如 7天：604800000）

## 验证方法

1. **IDE 检查：** config 包的爆红应该已经消失
2. **编译检查：**
   ```bash
   mvn clean compile
   ```
3. **启动应用：**
   ```bash
   mvn spring-boot:run
   ```

所有问题已修复！🎉
