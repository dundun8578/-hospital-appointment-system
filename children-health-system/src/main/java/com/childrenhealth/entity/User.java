package com.childrenhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（唯一）
     */
    @TableField("username")
    private String username;

    /**
     * 密码（BCrypt加密存储）
     * 注意：此字段不会在接口中返回
     */
    @TableField("password")
    private String password;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 角色：1=家长，2=医生，3=管理员
     */
    @TableField("role")
    private Integer role;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 状态：1=正常，0=禁用
     */
    @TableField("status")
    private Integer status;

    /**
     * 逻辑删除：0=未删除，1=已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * JWT Token（用于返回给前端）
     */
    @TableField(exist = false)
    private String token;
}
