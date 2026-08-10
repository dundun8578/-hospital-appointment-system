package com.hospital.common.interceptor;

import com.hospital.common.utils.IpBlacklistUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * IP黑名单拦截器
 * 拦截黑名单IP的请求
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Component
public class IpBlacklistInterceptor implements HandlerInterceptor {

    @Autowired
    private IpBlacklistUtil ipBlacklistUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);

        if (ipBlacklistUtil.isBlacklisted(clientIp)) {
            log.warn("黑名单IP请求被拦截: ip={}, uri={}", clientIp, request.getRequestURI());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write("{\"code\":403,\"message\":\"您的IP已被限制访问\",\"data\":null}");
            return false;
        }

        return true;
    }

    /**
     * 获取客户端真实IP
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
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
