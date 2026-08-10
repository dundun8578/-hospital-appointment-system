package com.hospital.auth.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具类
 * 使用BCrypt算法加密密码
 *
 * @author hospital
 * @date 2024-01-15
 */
@Component
public class PasswordEncoderUtil {

    /**
     * BCrypt密码编码器
     * strength参数：4-31，默认10，强度越高越安全但越慢
     */
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    /**
     * 密码加密
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 加密后的密码
     * @return true-匹配，false-不匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
