package com.hospital.auth.interceptor;

import com.hospital.auth.utils.JwtUtil;
import com.hospital.common.BusinessException;
import com.hospital.common.ResultCode;
import com.hospital.user.entity.User;
import com.hospital.user.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 * 验证Token并设置用户信息到SecurityContext
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * 请求前处理
     * 验证Token并设置用户信息
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return true-继续执行，false-拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS请求直接放行（CORS预检请求）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");

        // Token为空
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Token为空或格式错误: {}", token);
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 提取Token（去掉Bearer前缀）
        token = token.substring(7);

        try {
            // 1. 验证Token并提取用户名
            String username = jwtUtil.extractUsername(token);

            // 2. 从数据库查询用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                log.warn("Token中的用户不存在: {}", username);
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }

            // 3. 验证用户状态
            if (user.getStatus() != 1) {
                log.warn("用户已被禁用: {}", username);
                throw new BusinessException(ResultCode.FORBIDDEN);
            }

            // 4. 将用户信息存入ThreadLocal（供后续使用）
            UserContextHolder.setUser(user);

            log.debug("Token验证成功，用户: {}", username);

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
        } catch (Exception e) {
            log.error("Token验证异常", e);
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    /**
     * 请求完成后清理ThreadLocal
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}
