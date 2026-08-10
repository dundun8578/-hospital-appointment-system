package com.hospital.auth.annotation;

import java.lang.annotation.*;

/**
 * 登录用户注解
 * 用于从SecurityContext中获取当前登录用户
 *
 * @author hospital
 * @date 2024-01-15
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginUser {

    /**
     * 是否必须登录
     * true：必须登录，未登录抛出异常
     * false：可选登录，未登录返回null
     */
    boolean required() default true;
}
