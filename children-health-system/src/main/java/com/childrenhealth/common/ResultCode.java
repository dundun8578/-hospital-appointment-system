package com.childrenhealth.common;

import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),

    // 参数错误 4xx
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(40001, "缺少必要参数"),
    PARAM_FORMAT_ERROR(40002, "参数格式错误"),

    // 用户模块 401xx
    USER_NOT_FOUND(40101, "用户不存在"),
    USERNAME_EXISTS(40102, "用户名已存在"),
    EMAIL_EXISTS(40103, "邮箱已被注册"),
    PHONE_EXISTS(40104, "手机号已被注册"),
    PASSWORD_ERROR(40105, "密码错误"),
    USER_DISABLED(40106, "用户已被禁用"),
    USER_NOT_LOGIN(40107, "用户未登录"),
    TOKEN_INVALID(40108, "token无效或已过期"),

    // 权限模块 403xx
    PERMISSION_DENIED(40301, "权限不足"),

    // 服务器错误 5xx
    SERVER_ERROR(50000, "服务器内部错误"),
    DATABASE_ERROR(50001, "数据库操作失败"),

    ;

    private static final Map<Integer, ResultCode> map = new HashMap<>();

    static {
        for (ResultCode code : values()) {
            map.put(code.getCode(), code);
        }
    }

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ResultCode getByCode(Integer code) {
        return map.get(code);
    }
}
