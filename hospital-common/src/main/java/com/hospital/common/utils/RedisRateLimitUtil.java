package com.hospital.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis限流工具类
 * 基于Redis ZSet实现滑动窗口限流
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Component
public class RedisRateLimitUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 限流Key前缀
     */
    private static final String RATE_LIMIT_KEY_PREFIX = "rate:limit:%s";

    /**
     * 默认时间窗口（秒）
     */
    private static final int DEFAULT_TIME_WINDOW = 60;

    /**
     * 默认最大请求次数
     */
    private static final int DEFAULT_MAX_REQUESTS = 10;

    /**
     * 滑动窗口限流
     * 使用Redis ZSet记录请求时间戳，移除窗口外的记录，判断当前窗口内请求数
     *
     * @param key          限流标识
     * @param timeWindow   时间窗口（秒）
     * @param maxRequests  最大请求次数
     * @return true-限流通过，false-被限流
     */
    public boolean tryAcquire(String key, int timeWindow, int maxRequests) {
        String redisKey = String.format(RATE_LIMIT_KEY_PREFIX, key);
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - timeWindow * 1000L;

        try {
            // 1. 移除窗口外的记录
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);

            // 2. 统计当前窗口内的请求数
            Long count = redisTemplate.opsForZSet().zCard(redisKey);

            // 3. 判断是否超过限流阈值
            if (count != null && count >= maxRequests) {
                log.warn("请求被限流: key={}, count={}, max={}", key, count, maxRequests);
                return false;
            }

            // 4. 添加当前请求记录
            redisTemplate.opsForZSet().add(redisKey, String.valueOf(currentTime), currentTime);

            // 5. 设置过期时间（防止无限增长）
            redisTemplate.expire(redisKey, timeWindow, TimeUnit.SECONDS);

            log.debug("限流通过: key={}, count={}, max={}", key, count + 1, maxRequests);
            return true;

        } catch (Exception e) {
            log.error("限流异常: key={}", key, e);
            // 异常时默认放行
            return true;
        }
    }

    /**
     * 滑动窗口限流（使用默认配置）
     *
     * @param key 限流标识
     * @return true-限流通过，false-被限流
     */
    public boolean tryAcquire(String key) {
        return tryAcquire(key, DEFAULT_TIME_WINDOW, DEFAULT_MAX_REQUESTS);
    }

    /**
     * 滑动窗口限流
     *
     * @param key          限流标识
     * @param timeWindow   时间窗口（秒）
     * @param maxRequests  最大请求次数
     * @return true-限流通过，false-被限流
     */
    public boolean tryAcquire(String key, long timeWindow, int maxRequests) {
        return tryAcquire(key, (int) timeWindow, maxRequests);
    }
}
