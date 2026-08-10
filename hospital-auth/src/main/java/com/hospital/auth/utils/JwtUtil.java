package com.hospital.auth.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 * 负责Token的生成、验证、解析
 *
 * @author hospital
 * @date 2024-01-15
 */
@Component
public class JwtUtil {

    /**
     * JWT密钥（至少32位）
     */
    @Value("${jwt.secret:hospital-appointment-system-secret-key-2024}")
    private String secret;

    /**
     * Token有效期（毫秒）：默认7天
     */
    @Value("${jwt.expiration:604800000}")
    private Long expiration;

    /**
     * 从Token中提取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从Token中提取过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从Token中提取指定声明
     *
     * @param token           JWT Token
     * @param claimsResolver 声明解析函数
     * @param <T>            返回类型
     * @return 声明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从Token中提取所有声明
     *
     * @param token JWT Token
     * @return Claims对象
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 检查Token是否过期
     *
     * @param token JWT Token
     * @return true-已过期，false-未过期
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 为指定用户生成Token
     *
     * @param userDetails 用户详情
     * @return JWT Token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // 添加用户ID到claims
        if (userDetails instanceof com.hospital.user.entity.User) {
            claims.put("userId", ((com.hospital.user.entity.User) userDetails).getUserId());
        }
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 为指定用户生成Token（包含额外信息）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT Token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims, username);
    }

    /**
     * 创建Token
     *
     * @param claims  自定义声明
     * @param subject 主题（用户名）
     * @return JWT Token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 验证Token有效性
     *
     * @param token         JWT Token
     * @param userDetails  用户详情
     * @return true-有效，false-无效
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * 获取签名密钥
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
