package com.hospital.auth.interceptor;

import com.hospital.user.entity.User;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户上下文工具类
 * 使用ThreadLocal存储当前请求的用户信息
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
public class UserContextHolder {

    /**
     * ThreadLocal存储当前用户
     */
    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户
     *
     * @param user 用户信息
     */
    public static void setUser(User user) {
        if (user != null) {
            USER_HOLDER.set(user);
            log.debug("设置当前用户: userId={}, username={}", user.getUserId(), user.getUsername());
        }
    }

    /**
     * 获取当前用户
     *
     * @return 用户信息
     */
    public static User getUser() {
        User user = USER_HOLDER.get();
        if (user == null) {
            log.warn("当前请求未登录");
            throw new com.hospital.common.BusinessException(com.hospital.common.ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        return getUser().getUserId();
    }

    /**
     * 清理ThreadLocal
     * 防止内存泄漏
     */
    public static void clear() {
        USER_HOLDER.remove();
        log.debug("清理用户上下文");
    }
}
