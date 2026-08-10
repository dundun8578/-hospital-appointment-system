package com.hospital.appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 预约查询DTO
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
public class AppointmentQueryDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 预约状态：1-待就诊 2-已就诊 3-已取消 4-已过期 5-已爽约
     */
    private Integer status;

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;
}
