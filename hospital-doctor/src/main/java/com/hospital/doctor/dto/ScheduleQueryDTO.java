package com.hospital.doctor.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 排班查询条件DTO
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
public class ScheduleQueryDTO {

    /**
     * 医院ID
     */
    private Long hospitalId;

    /**
     * 科室ID
     */
    private Long departmentId;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 查询日期
     */
    private LocalDate workDate;

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;
}
