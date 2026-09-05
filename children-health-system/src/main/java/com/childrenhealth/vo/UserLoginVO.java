package com.childrenhealth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 登录返回对象
 */
@Data
public class UserLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户信息
     */
    private Map<String, Object> userInfo;

    /**
     * 权限列表
     */
    private Map<String, String> permissions;
}
