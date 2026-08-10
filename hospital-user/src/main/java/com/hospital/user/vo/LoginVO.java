package com.hospital.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应VO
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@Schema(description = "登录响应")
public class LoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "用户信息")
    private UserVO userInfo;

    /**
     * 构造函数
     *
     * @param token    JWT Token
     * @param userInfo 用户信息
     */
    public LoginVO(String token, UserVO userInfo) {
        this.token = token;
        this.userInfo = userInfo;
    }
}
