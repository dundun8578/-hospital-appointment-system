package com.hospital.common.config;

import com.hospital.auth.interceptor.JwtInterceptor;
import com.hospital.common.annotation.RateLimit;
import com.hospital.common.interceptor.IpBlacklistInterceptor;
import com.hospital.common.interceptor.RateLimitInterceptor;
import com.hospital.common.interceptor.RequestLogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 统一配置所有拦截器
 *
 * @author hospital
 * @date 2024-01-15
 */
@Configuration
public class RateLimitWebConfig implements WebMvcConfigurer {

    @Autowired(required = false)
    private JwtInterceptor jwtInterceptor;

    @Autowired(required = false)
    private RateLimitInterceptor rateLimitInterceptor;

    @Autowired(required = false)
    private IpBlacklistInterceptor ipBlacklistInterceptor;

    @Autowired(required = false)
    private RequestLogInterceptor requestLogInterceptor;

    /**
     * 配置拦截器
     * 注意：拦截器执行顺序与添加顺序相反
     * 先添加的拦截器后执行
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. IP黑名单拦截器（最先检查）
        if (ipBlacklistInterceptor != null) {
            registry.addInterceptor(ipBlacklistInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(
                            "/doc.html",
                            "/doc.html/**",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/favicon.ico",
                            "/error"
                    );
        }

        // 2. 请求日志拦截器
        if (requestLogInterceptor != null) {
            registry.addInterceptor(requestLogInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(
                            "/doc.html",
                            "/doc.html/**",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/favicon.ico",
                            "/error"
                    );
        }

        // 3. 限流拦截器
        if (rateLimitInterceptor != null) {
            registry.addInterceptor(rateLimitInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(
                            "/user/login",
                            "/user/register",
                            "/doc.html",
                            "/doc.html/**",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/favicon.ico",
                            "/error"
                    );
        }

        // 4. JWT认证拦截器（最后检查）
        if (jwtInterceptor != null) {
            registry.addInterceptor(jwtInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(
                            "/user/login",
                            "/user/register",
                            "/doc.html",
                            "/doc.html/**",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/favicon.ico",
                            "/error"
                    );
        }
    }

    /**
     * 配置跨域
     *
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
