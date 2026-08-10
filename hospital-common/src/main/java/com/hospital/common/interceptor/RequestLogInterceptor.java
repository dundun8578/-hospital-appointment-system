package com.hospital.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 请求日志拦截器
 * 记录所有请求日志
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();

        // 将开始时间存入request
        request.setAttribute("startTime", startTime);

        // 记录请求信息
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIp(request);

        log.info("请求开始: method={}, uri={}, queryString={}, ip={}",
                method, uri, queryString, clientIp);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        log.info("请求结束: method={}, uri={}, status={}, duration={}ms",
                method, uri, status, duration);

        // 慢请求告警
        if (duration > 1000) {
            log.warn("慢请求告警: method={}, uri={}, duration={}ms", method, uri, duration);
        }
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
