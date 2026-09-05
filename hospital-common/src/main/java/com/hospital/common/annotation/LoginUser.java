package com.hospital.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登录用户注解
 * 用于标记需要注入当前登录用户的方法参数
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
