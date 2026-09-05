package com.hospital.common.jwt;

import com.hospital.common.BusinessException;
import com.hospital.common.ResultCode;
import lombok.extern.slf4j.Slf4j;

/**
 * 当前登录用户上下文（ThreadLocal，请求结束由JwtInterceptor清理）
 */
@Slf4j
public class UserContextHolder {

    private static final ThreadLongHolder HOLDER = new ThreadLongHolder();

    private UserContextHolder() {
    }

    public static void set(Long userId, String username) {
        HOLDER.userId.set(userId);
        HOLDER.username.set(username);
    }

    /**
     * 获取当前登录用户ID，未登录抛出未认证异常
     */
    public static Long getUserId() {
        Long userId = HOLDER.userId.get();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    /**
     * 获取当前登录用户ID，未登录返回null
     */
    public static Long getUserIdOrNull() {
        return HOLDER.userId.get();
    }

    public static String getUsername() {
        return HOLDER.username.get();
    }

    public static void clear() {
        HOLDER.userId.remove();
        HOLDER.username.remove();
    }

    private static class ThreadLongHolder {
        private final ThreadLocal<Long> userId = new ThreadLocal<>();
        private final ThreadLocal<String> username = new ThreadLocal<>();
    }
}
