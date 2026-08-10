package com.hospital.appointment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约VO
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@Schema(description = "预约信息")
public class AppointmentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "排班ID")
    private Long scheduleId;

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "医生职称")
    private String doctorTitle;

    @Schema(description = "医院ID")
    private Long hospitalId;

    @Schema(description = "医院名称")
    private String hospitalName;

    @Schema(description = "医院地址")
    private String hospitalAddress;

    @Schema(description = "科室ID")
    private Long departmentId;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "预约日期")
    private LocalDate workDate;

    @Schema(description = "时段：1-上午 2-下午 3-晚上")
    private Integer timeSlot;

    @Schema(description = "时段名称")
    private String timeSlotName;

    @Schema(description = "就诊人姓名")
    private String patientName;

    @Schema(description = "就诊人手机号")
    private String patientPhone;

    @Schema(description = "挂号费（元）")
    private BigDecimal price;

    @Schema(description = "预约状态：1-待就诊 2-已就诊 3-已取消 4-已过期 5-已爽约")
    private Integer status;

    @Schema(description = "预约状态名称")
    private String statusName;

    @Schema(description = "就诊状态：0-未就诊 1-已就诊")
    private Integer visitStatus;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
