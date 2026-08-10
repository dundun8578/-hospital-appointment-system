package com.hospital.appointment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约实体类
 * 对应数据库表：appointment（核心表）
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@TableName("appointment")
public class Appointment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 预约ID，主键
     */
    @TableId(value = "appointment_id", type = IdType.AUTO)
    private Long appointmentId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 排班ID
     */
    private Long scheduleId;

    /**
     * 医生ID（冗余字段）
     */
    private Long doctorId;

    /**
     * 医院ID（冗余字段）
     */
    private Long hospitalId;

    /**
     * 科室ID（冗余字段）
     */
    private Long departmentId;

    /**
     * 预约日期
     */
    private LocalDate workDate;

    /**
     * 时段：1-上午 2-下午 3-晚上
     */
    private Integer timeSlot;

    /**
     * 就诊人姓名
     */
    private String patientName;

    /**
     * 就诊人手机号
     */
    private String patientPhone;

    /**
     * 就诊人身份证号
     */
    private String patientIdCard;

    /**
     * 证件类型：1-身份证 2-护照 3-港澳通行证
     */
    private Integer idCardType;

    /**
     * 状态：1-待就诊 2-已就诊 3-已取消 4-已过期 5-已爽约
     */
    private Integer status;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 就诊状态：0-未就诊 1-已就诊
     */
    private Integer visitStatus;

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
