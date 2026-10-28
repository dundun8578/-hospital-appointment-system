package com.hospital.common.jwt;

import com.hospital.common.BusinessException;
import com.hospital.common.ResultCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT登录校验拦截器（无状态）
 * 只解析Token不查库：校验通过后把userId放入请求属性（供@RequestAttribute使用）
 * 并写入UserContextHolder（供业务代码获取当前用户），请求结束后清理
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS预检请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Token为空或格式错误");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        token = token.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            // 供@RequestAttribute("userId")与业务代码使用
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            UserContextHolder.set(userId, username);
            log.debug("Token验证成功, userId={}, username={}", userId, username);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期");
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
        } catch (SignatureException e) {
            log.warn("Token签名验证失败");
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        } catch (MalformedJwtException | UnsupportedJwtException e) {
            log.warn("Token格式错误");
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token验证异常", e);
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}
