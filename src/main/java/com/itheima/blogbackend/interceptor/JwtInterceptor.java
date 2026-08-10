package com.itheima.blogbackend.interceptor;

import com.itheima.blogbackend.common.BusinessException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器
 * 用于验证请求中的 JWT Token
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public JwtInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 从请求头中获取 Token
        String token = request.getHeader("Authorization");

        // 检查 Token 是否存在
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(401, "未登录，请先登录");
        }

        // 移除 "Bearer " 前缀（如果存在）
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 解析 Token
            Claims claims = jwtUtils.parseToken(token);

            // 将用户信息存入 request，方便后续使用
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("username", claims.get("username"));

            return true;
        } catch (Exception e) {
            throw new BusinessException(401, "Token 无效或已过期");
        }
    }
}
