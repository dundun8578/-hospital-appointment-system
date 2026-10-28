package com.hospital.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.UUID;

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

    private DefaultRedisScript<Long> slidingWindowScript;

    @PostConstruct
    public void init() {
        slidingWindowScript = new DefaultRedisScript<>();
        slidingWindowScript.setScriptSource(new ResourceScriptSource(
                new ClassPathResource("lua/sliding_window_rate_limit.lua")));
        slidingWindowScript.setResultType(Long.class);
    }

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

        try {
            String requestId = currentTime + ":" + UUID.randomUUID();
            Long result = redisTemplate.execute(
                    slidingWindowScript,
                    Collections.singletonList(redisKey),
                    String.valueOf(currentTime),
                    String.valueOf(timeWindow * 1000L),
                    String.valueOf(maxRequests),
                    requestId);
            boolean allowed = result != null && result == 1L;
            if (!allowed) {
                log.warn("请求被限流: key={}, max={}", key, maxRequests);
            }
            return allowed;

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
