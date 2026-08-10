package com.hospital.common.interceptor;

import com.hospital.common.annotation.RateLimit;
import com.hospital.common.utils.RedisRateLimitUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

/**
 * 限流拦截器
 * 基于Redis滑动窗口实现接口限流
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisRateLimitUtil redisRateLimitUtil;

    /**
     * 预处理
     * 检查是否超过限流阈值
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return true-继续执行，false-拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 只拦截方法级别的Handler
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Method method = handlerMethod.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 没有@RateLimit注解的方法放行
        if (rateLimit == null || !rateLimit.enabled()) {
            return true;
        }

        // 获取客户端IP
        String clientIp = getClientIp(request);
        String key = String.format("%s:%s:%s",
                rateLimit.keyPrefix(),
                method.getDeclaringClass().getSimpleName(),
                method.getName());

        // 限流判断
        boolean allowed = redisRateLimitUtil.tryAcquire(
                key + ":" + clientIp,
                rateLimit.timeWindow(),
                rateLimit.maxRequests()
        );

        if (!allowed) {
            log.warn("请求被限流: ip={}, key={}, message={}",
                    clientIp, key, rateLimit.message());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(429);
            response.getWriter().write("{\"code\":429,\"message\":\"" + rateLimit.message() + "\",\"data\":null}");
            return false;
        }

        return true;
    }

    /**
     * 获取客户端真实IP
     * 处理Nginx反向代理等情况
     *
     * @param request 请求
     * @return 客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
