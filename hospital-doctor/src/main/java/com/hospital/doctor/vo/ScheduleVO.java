package com.hospital.doctor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班VO
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@Schema(description = "排班信息")
public class ScheduleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "排班ID")
    private Long scheduleId;

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "医生职称")
    private String doctorTitle;

    @Schema(description = "医生头像")
    private String doctorAvatar;

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

    @Schema(description = "排班日期")
    private LocalDate workDate;

    @Schema(description = "时段：1-上午 2-下午 3-晚上")
    private Integer timeSlot;

    @Schema(description = "时段名称")
    private String timeSlotName;

    @Schema(description = "总号源数")
    private Integer totalCount;

    @Schema(description = "剩余号源数")
    private Integer availableCount;

    @Schema(description = "挂号费（元）")
    private BigDecimal price;

    @Schema(description = "排班状态")
    private Integer status;

    @Schema(description = "排班状态名称")
    private String statusName;

    @Schema(description = "是否可预约")
    private Boolean canAppointment;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
