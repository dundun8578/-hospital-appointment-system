package com.hospital.appointment.mq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 预约消息DTO
 * 用于RabbitMQ消息传递
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
public class AppointmentMessageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 预约ID
     */
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
     * 医生ID
     */
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
     * 预约日期
     */
    private String workDate;

    /**
     * 时段
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
     * 挂号费
     */
    private java.math.BigDecimal price;

    /**
     * 消息创建时间
     */
    private Long createTime;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;
}
