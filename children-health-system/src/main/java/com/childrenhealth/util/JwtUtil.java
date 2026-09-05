package com.childrenhealth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.childrenhealth.entity.User;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * Token有效期：7天
 */
@Component
public class JwtUtil {

    /**
     * Token过期时间：7天（单位：毫秒）
     */
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;

    /**
     * JWT签名密钥（必须与配置文件中的secret一致）
     */
    private static final String SECRET = "children-health-system-secret-key-2024-12306";

    /**
     * 生成JWT Token
     *
     * @param user 用户信息
     * @return JWT Token字符串
     */
    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);

            Map<String, Object> header = new HashMap<>();
            header.put("typ", "JWT");
            header.put("alg", "HS256");

            return JWT.create()
                    .withHeader(header)
                    .withIssuer("children-health-system")
                    .withSubject(String.valueOf(user.getId()))
                    .withClaim("username", user.getUsername())
                    .withClaim("role", user.getRole())
                    .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .withIssuedAt(new Date())
                    .sign(algorithm);
        } catch (Exception e) {
            throw new RuntimeException("生成Token失败", e);
        }
    }

    /**
     * 验证Token并返回解码后的JWT
     *
     * @param token JWT Token
     * @return 解码后的JWT
     */
    public DecodedJWT verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("children-health-system")
                    .build();
            return verifier.verify(token);
        } catch (Exception e) {
            throw new RuntimeException("Token验证失败", e);
        }
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return Long.valueOf(jwt.getSubject());
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("username").asString();
    }

    /**
     * 从Token中获取用户角色
     *
     * @param token JWT Token
     * @return 角色编码
     */
    public Integer getRoleFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("role").asInt();
    }

    /**
     * 检查Token是否过期
     *
     * @param token JWT Token
     * @return true=已过期，false=未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            Date expiresAt = jwt.getExpiresAt();
            return expiresAt.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
