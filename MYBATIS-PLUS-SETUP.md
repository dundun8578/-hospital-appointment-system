# MyBatis-Plus Mapper 使用说明

## 问题排查

如果你遇到Mapper映射"爆红"的问题，请检查以下几点：

### 1. 扫描路径配置
在主启动类上使用 `@MapperScan` 注解指定Mapper接口的包路径：

```java
@SpringBootApplication
@MapperScan("com.itheima.blogbackend.mapper") // 必须是你的mapper包路径
public class BlogBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogBackendApplication.class, args);
    }
}
```

### 2. Mapper接口定义
每个Mapper接口必须：
- 继承 `BaseMapper<实体类>`
- 添加 `@Mapper` 注解
- 放在正确的包下

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper提供了insert/delete/update/select等基础方法
}
```

### 3. 配置文件检查
确保 `application.properties` 包含MyBatis-Plus配置：

```properties
# Mapper XML文件位置
mybatis-plus.mapper-locations=classpath:mapper/*.xml

# 实体类包路径
mybatis-plus.type-aliases-package=com.itheima.blogbackend.entity

# 开启驼峰命名转换（推荐）
mybatis-plus.configuration.map-underscore-to-camel-case=true
```

### 4. 依赖检查
确保pom.xml包含mybatis-plus依赖：

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.6</version>
</dependency>
```

## 已修复的问题

1. ✅ 修复了 `@MapperScan` 路径错误
   - 原来：`com.itheima.mapper` ❌
   - 现在：`com.itheima.blogbackend.mapper` ✅

2. ✅ 添加了完整的MyBatis-Plus配置

3. ✅ 创建了示例代码（User实体、UserMapper、UserService）

## 使用建议

### 方式一：使用Service层（推荐）
```java
@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    // 直接使用父类方法，无需编写SQL
    public User getUserById(Long id) {
        return getById(id);
    }
}
```

### 方式二：直接注入Mapper
```java
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
}
```

### 方式三：使用XML自定义SQL
当需要复杂SQL时，在 `src/main/resources/mapper/` 下创建对应的XML文件。

## IDE配置建议

如果IDE仍然显示错误，尝试：

1. **刷新Maven依赖**：在IDEA中右键项目 → Maven → Reload Project
2. **清理缓存**：File → Invalidate Caches / Restart
3. **确保项目已正确导入**：检查pom.xml没有错误标记
