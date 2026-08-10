package com.hospital.doctor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建排班请求DTO
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
public class ScheduleCreateDTO {

    /**
     * 医生ID
     */
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    /**
     * 排班日期
     */
    @NotNull(message = "排班日期不能为空")
    private LocalDate workDate;

    /**
     * 时段：1-上午 2-下午 3-晚上
     */
    @NotNull(message = "时段不能为空")
    private Integer timeSlot;

    /**
     * 总号源数
     */
    @NotNull(message = "号源数量不能为空")
    @Positive(message = "号源数量必须大于0")
    private Integer totalCount;

    /**
     * 挂号费（元）
     */
    @NotNull(message = "挂号费不能为空")
    @Positive(message = "挂号费必须大于0")
    private BigDecimal price;
}
