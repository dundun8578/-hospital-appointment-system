package com.childrenhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.childrenhealth.common.BusinessException;
import com.childrenhealth.common.ResultCode;
import com.childrenhealth.entity.User;
import com.childrenhealth.mapper.UserMapper;
import com.childrenhealth.service.UserService;
import com.childrenhealth.util.JwtUtil;
import com.childrenhealth.vo.UserLoginVO;
import com.childrenhealth.vo.UserRegisterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     */
    @Override
    public UserRegisterVO register(User user) {
        // 1. 校验用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 2. 校验邮箱是否已存在
        if (user.getEmail() != null && existsByEmail(user.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // 3. 校验手机号是否已存在
        if (user.getPhone() != null && existsByPhone(user.getPhone())) {
            throw new BusinessException(ResultCode.PHONE_EXISTS);
        }

        // 4. 密码加密
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 5. 设置默认角色（1=家长）
        if (user.getRole() == null) {
            user.setRole(1);
        }

        // 6. 设置默认状态（1=正常）
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        // 7. 保存到数据库
        boolean saved = save(user);
        if (!saved) {
            throw new BusinessException(ResultCode.DATABASE_ERROR);
        }

        // 8. 生成JWT Token
        String token = jwtUtil.generateToken(user);

        // 9. 构建返回结果（不含密码）
        UserRegisterVO vo = new UserRegisterVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRegisterTime(user.getCreateTime());
        vo.setToken(token);
        vo.setRole(user.getRole());

        // 根据角色设置权限
        Map<String, String> permissions = getPermissionsByRole(user.getRole());
        vo.setPermissions(permissions);

        return vo;
    }

    /**
     * 用户登录
     */
    @Override
    public UserLoginVO login(String username, String password) {
        // 1. 查询用户
        User user = getByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 4. 生成JWT Token
        String token = jwtUtil.generateToken(user);

        // 5. 构建返回结果（不含密码）
        UserLoginVO vo = new UserLoginVO();
        vo.setToken(token);

        // 构建userInfo
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("role", user.getRole());
        userInfo.put("avatar", user.getAvatar());
        vo.setUserInfo(userInfo);

        // 根据角色设置权限
        Map<String, String> permissions = getPermissionsByRole(user.getRole());
        vo.setPermissions(permissions);

        return vo;
    }

    /**
     * 根据用户名查询用户
     */
    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
                .eq(User::getDeleted, 0)
                .last("LIMIT 1");
        return getOne(wrapper);
    }

    /**
     * 检查用户名是否存在
     */
    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
                .eq(User::getDeleted, 0)
                .select(User::getId)
                .last("LIMIT 1");
        return getOne(wrapper) != null;
    }

    /**
     * 检查邮箱是否存在
     */
    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email.trim())
                .eq(User::getDeleted, 0)
                .select(User::getId)
                .last("LIMIT 1");
        return getOne(wrapper) != null;
    }

    /**
     * 检查手机号是否存在
     */
    @Override
    public boolean existsByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone.trim())
                .eq(User::getDeleted, 0)
                .select(User::getId)
                .last("LIMIT 1");
        return getOne(wrapper) != null;
    }

    /**
     * 根据角色获取权限列表
     */
    private Map<String, String> getPermissionsByRole(Integer role) {
        Map<String, String> permissions = new HashMap<>();

        switch (role) {
            case 1: // 家长
                permissions.put("child:view", "查看儿童档案");
                permissions.put("child:edit", "编辑儿童档案");
                permissions.put("health:record", "健康数据录入");
                permissions.put("health:view", "查看健康报告");
                permissions.put("consultation:create", "发起在线问诊");
                permissions.put("consultation:view", "查看问诊记录");
                break;

            case 2: // 医生
                permissions.put("patient:view", "查看患者列表");
                permissions.put("patient:manage", "管理患者档案");
                permissions.put("health:assess", "健康评估");
                permissions.put("health:plan", "制定健康方案");
                permissions.put("consultation:reply", "回复问诊咨询");
                permissions.put("consultation:view", "查看问诊记录");
                permissions.put("report:view", "查看健康报告");
                break;

            case 3: // 管理员
                permissions.put("user:manage", "用户管理");
                permissions.put("user:audit", "内容审核");
                permissions.put("data:statistics", "数据统计");
                permissions.put("system:config", "系统配置");
                permissions.put("doctor:manage", "医生管理");
                break;

            default:
                throw new BusinessException("未知用户角色");
        }

        return permissions;
    }
}
