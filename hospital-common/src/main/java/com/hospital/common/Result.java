package com.hospital.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果封装
 *
 * @param <T> 响应数据类型
 * @author hospital
 * @date 2024-01-15
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 成功响应
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 响应结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 成功响应（无数据）
     *
     * @return 响应结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败响应
     *
     * @param code    响应码
     * @param message 响应消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 失败响应（使用响应码枚举）
     *
     * @param resultCode 响应码枚举
     * @param <T>        数据类型
     * @return 响应结果
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 失败响应（自定义消息）
     *
     * @param resultCode 响应码枚举
     * @param message    自定义消息
     * @param <T>        数据类型
     * @return 响应结果
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return error(resultCode.getCode(), message);
    }
}
