package com.hospital.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * IP黑名单工具
 * 用于封禁恶意IP
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Component
public class IpBlacklistUtil {

    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_KEY_PREFIX = "ip:blacklist:";
    private static final long DEFAULT_EXPIRE_DAYS = 7;

    public IpBlacklistUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 添加IP到黑名单
     *
     * @param ip IP地址
     */
    public void addToBlacklist(String ip) {
        String key = BLACKLIST_KEY_PREFIX + ip;
        redisTemplate.opsForValue().set(key, "1", DEFAULT_EXPIRE_DAYS, TimeUnit.DAYS);
        log.warn("IP已加入黑名单: ip={}, expireDays={}", ip, DEFAULT_EXPIRE_DAYS);
    }

    /**
     * 从黑名单移除IP
     *
     * @param ip IP地址
     */
    public void removeFromBlacklist(String ip) {
        String key = BLACKLIST_KEY_PREFIX + ip;
        redisTemplate.delete(key);
        log.info("IP已从黑名单移除: ip={}", ip);
    }

    /**
     * 检查IP是否在黑名单中
     *
     * @param ip IP地址
     * @return true-在黑名单中，false-不在
     */
    public boolean isBlacklisted(String ip) {
        String key = BLACKLIST_KEY_PREFIX + ip;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
