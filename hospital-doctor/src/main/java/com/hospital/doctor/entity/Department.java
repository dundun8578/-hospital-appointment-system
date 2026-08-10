package com.hospital.doctor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 科室实体类
 * 对应数据库表：department
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@TableName("department")
public class Department implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 科室ID，主键
     */
    @TableId(value = "department_id", type = IdType.AUTO)
    private Long departmentId;

    /**
     * 医院ID
     */
    private Long hospitalId;

    /**
     * 科室名称
     */
    private String departmentName;

    /**
     * 科室编码
     */
    private String departmentCode;

    /**
     * 父科室ID，0表示一级科室
     */
    private Long parentId;

    /**
     * 科室描述
     */
    private String description;

    /**
     * 科室位置
     */
    private String location;

    /**
     * 状态：1-正常 0-停用
     */
    private Integer status;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
