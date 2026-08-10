package com.hospital.common;

import lombok.Getter;

/**
 * 响应码枚举
 * 统一管理接口返回的状态码
 *
 * @author hospital
 * @date 2024-01-15
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 参数错误
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(40001, "缺少必要参数"),
    PARAM_INVALID(40002, "参数格式不正确"),

    // 认证相关
    UNAUTHORIZED(401, "未授权，请先登录"),
    TOKEN_EXPIRED(40101, "token已过期"),
    TOKEN_INVALID(40102, "token无效"),
    FORBIDDEN(403, "权限不足"),

    // 业务错误
    USER_NOT_FOUND(40401, "用户不存在"),
    HOSPITAL_NOT_FOUND(40402, "医院不存在"),
    DEPARTMENT_NOT_FOUND(40403, "科室不存在"),
    DOCTOR_NOT_FOUND(40404, "医生不存在"),
    SCHEDULE_NOT_FOUND(40405, "排班不存在"),
    APPOINTMENT_NOT_FOUND(40406, "预约记录不存在"),

    // 预约相关错误
    STOCK_NOT_ENOUGH(10001, "号源不足"),
    APPOINTMENT_REPEAT(10002, "您已预约过该号源"),
    APPOINTMENT_TIME_CONFLICT(10003, "预约时间冲突"),
    APPOINTMENT_CANCELLED(10004, "预约已取消"),
    APPOINTMENT_COMPLETED(10005, "预约已完成"),

    // 系统错误
    SYSTEM_ERROR(500, "系统异常，请稍后重试"),
    DATABASE_ERROR(50001, "数据库操作失败"),
    REDIS_ERROR(50002, "缓存服务异常"),
    MQ_ERROR(50003, "消息队列服务异常"),

    // 限流相关
    RATE_LIMIT_EXCEEDED(429, "请求过于频繁，请稍后再试");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
