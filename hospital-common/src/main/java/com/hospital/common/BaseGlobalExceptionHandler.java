package com.hospital.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 模块化全局异常处理器基类
 * 各模块可以继承此类添加特定异常处理
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@RestControllerAdvice
public class BaseGlobalExceptionHandler extends GlobalExceptionHandler {
    // 可以在此添加模块特定的异常处理
}
