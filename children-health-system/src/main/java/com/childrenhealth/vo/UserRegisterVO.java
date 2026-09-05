package com.childrenhealth.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 注册返回对象
 */
@Data
public class UserRegisterVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * JWT Token
     */
    private String token;

    /**
     * 角色：1=家长，2=医生，3=管理员
     */
    private Integer role;

    /**
     * 权限列表
     */
    private Map<String, String> permissions;
}
