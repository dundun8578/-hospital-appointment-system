package com.hospital.doctor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医生实体类
 * 对应数据库表：doctor
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@TableName("doctor")
public class Doctor implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 医生ID，主键
     */
    @TableId(value = "doctor_id", type = IdType.AUTO)
    private Long doctorId;

    /**
     * 医院ID
     */
    private Long hospitalId;

    /**
     * 科室ID
     */
    private Long departmentId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 医生编码
     */
    private String doctorCode;

    /**
     * 职称
     */
    private String title;

    /**
     * 擅长领域
     */
    private String specialty;

    /**
     * 医生简介
     */
    private String description;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 挂号费（元）
     */
    private BigDecimal price;

    /**
     * 从业年限
     */
    private Integer experience;

    /**
     * 学历
     */
    private String education;

    /**
     * 状态：1-正常出诊 0-停诊
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
