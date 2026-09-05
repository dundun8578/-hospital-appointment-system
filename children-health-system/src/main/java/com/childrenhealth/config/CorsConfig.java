package com.childrenhealth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.List;

/**
 * 跨域配置
 * 允许 localhost:12306 访问
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的源（生产环境应该配置具体域名）
        config.setAllowedOrigins(List.of("http://localhost:12306", "http://127.0.0.1:12306"));

        // 允许的请求方法
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 允许的请求头
        config.setAllowedHeaders(List.of("*"));

        // 允许的响应头
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));

        // 允许携带凭证
        config.setAllowCredentials(true);

        // 预检请求有效期（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
