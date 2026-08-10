package com.hospital.common.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * 基于Redis滑动窗口实现方法级限流
 *
 * @author hospital
 * @date 2024-01-15
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流Key前缀
     */
    String keyPrefix() default "rate:limit";

    /**
     * 时间窗口（秒）
     */
    int timeWindow() default 60;

    /**
     * 最大请求次数
     */
    int maxRequests() default 10;

    /**
     * 限流提示消息
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * 是否启用限流
     */
    boolean enabled() default true;
}
